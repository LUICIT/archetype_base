package com.luis_r_aguilar.baseproject.service;

import com.luis_r_aguilar.baseproject.config.AppProperties;
import com.luis_r_aguilar.baseproject.converter.GenericConverter;
import com.luis_r_aguilar.baseproject.domain.entity.BaseUserEntity;
import com.luis_r_aguilar.baseproject.domain.repository.BaseUserRepository;
import com.luis_r_aguilar.baseproject.security.JwtUtil;
import com.luis_r_aguilar.baseproject.validation.EmailValidationModel;
import com.luis_r_aguilar.baseproject.web.model.BaseUserModel;
import com.luis_r_aguilar.baseproject.web.model.LoginModel;
import com.luis_r_aguilar.baseproject.web.model.RegisterBaseUserModel;
import com.luis_r_aguilar.baseproject.web.model.TokenModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private static final int LOCK_HOURS = 1;

    private final GenericConverter<BaseUserEntity, BaseUserModel> userConverter =
            new GenericConverter<>(BaseUserEntity::new, BaseUserModel::new);

    private final JwtUtil jwtUtil;
    private final Validator validator;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final BaseUserRepository baseUserRepository;
    private final AuthenticationManager authenticationManager;

    private final AuthService self;

    protected String usernameStr = "username";
    protected String emailStr = "email";

    public AuthService(JwtUtil jwtUtil,
                       Validator validator,
                       AppProperties appProperties,
                       PasswordEncoder passwordEncoder,
                       BaseUserRepository baseUserRepository,
                       AuthenticationManager authenticationManager,
                       @Lazy AuthService self) {
        this.jwtUtil = jwtUtil;
        this.validator = validator;
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.baseUserRepository = baseUserRepository;
        this.authenticationManager = authenticationManager;
        this.self = self;
    }

    @Transactional
    public BaseUserModel registerUser(RegisterBaseUserModel registerBaseUserModel) throws BindException {

        String baseUserModelStr = "baseUserModel";
        BindException errors = new BindException(registerBaseUserModel, baseUserModelStr);

        if (usernameStr.equalsIgnoreCase(appProperties.getSecurity().getLoginIdentifier())) {
            String username = registerBaseUserModel.getUsername();
            if (username == null || username.isBlank()) {
                errors.addError(new FieldError(baseUserModelStr, usernameStr, "Username is required"));
                throw errors;
            } else if (baseUserRepository.existsByUsernameAndDeletedAtIsNull(username.toLowerCase())) {
                errors.addError(new FieldError(baseUserModelStr, usernameStr, "Username already exists"));
                throw errors;
            }
        } else {
            String email = registerBaseUserModel.getEmail();
            if (email == null || email.isBlank()) {
                errors.addError(new FieldError(baseUserModelStr, emailStr, "Email is required"));
                throw errors;
            } else if (baseUserRepository.existsByEmailAndDeletedAtIsNull(email.toLowerCase())) {
                errors.addError(new FieldError(baseUserModelStr, emailStr, "Email already exists"));
                throw errors;
            }
        }

        String encryptedPassword = passwordEncoder.encode(registerBaseUserModel.getPassword());

        BaseUserEntity newUser = getNewBaseUserEntity(registerBaseUserModel, encryptedPassword);

        baseUserRepository.save(newUser);
        LOG.debug("Created Information for User: {}", newUser);

        return userConverter.aModelo(newUser);
    }

    private static @NonNull BaseUserEntity getNewBaseUserEntity(RegisterBaseUserModel registerBaseUserModel, String encryptedPassword) {
        BaseUserEntity newUser = new BaseUserEntity();
        newUser.setId(0L);
        newUser.setName(registerBaseUserModel.getName());
        newUser.setUsername(registerBaseUserModel.getUsername());
        newUser.setEmail(registerBaseUserModel.getEmail());
        newUser.setPassword(encryptedPassword);

        if (newUser.getUsername() != null) {
            newUser.setUsername(newUser.getUsername().toLowerCase());
        }

        if (newUser.getEmail() != null) {
            newUser.setEmail(newUser.getEmail().toLowerCase());
        }
        return newUser;
    }

    @Transactional
    public TokenModel login(LoginModel loginModel) throws BindException {

        BaseUserEntity userEntity = validateLoginIdentifier(loginModel);

        if (userEntity != null) {
            if (isLocked(userEntity)) {
                throw new BadCredentialsException("Invalid credentials");
            }
            applyBackoffIfNeeded(userEntity);
        }

        try {
            // 3) Autentica con Spring Security (username/email)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginModel.getUsername(), loginModel.getPassword())
            );

            // 4) Éxito: resetea contadores si el usuario existe
            if (userEntity != null) {
                resetFailures(userEntity);
                baseUserRepository.save(userEntity);
            }

            // 5) Genera JWT
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            assert principal != null;
            String token = jwtUtil.generateToken(principal);

            return new TokenModel(token, "Bearer");
        } catch (BadCredentialsException ex) {

            // 6) Fallo: incrementa contadores si el usuario existe
            // Nota: como aquí relanzamos una RuntimeException, la transacción actual haría rollback.
            // Por eso persistimos el intento fallido en una transacción nueva.
            if (userEntity != null && userEntity.getId() != null) {
                self.recordFailedLogin(userEntity.getId());
            }

            // Respuesta uniforme
            throw ex;
        }
    }

    private BaseUserEntity validateLoginIdentifier(LoginModel loginModel) throws BindException {
        String loginModelStr = "loginModel";
        BindException errors = new BindException(loginModel, loginModelStr);

        if (usernameStr.equalsIgnoreCase(appProperties.getSecurity().getLoginIdentifier())) {
            String username = loginModel.getUsername();
            if (username == null || username.isBlank()) {
                errors.addError(new FieldError(loginModelStr, usernameStr, "Username is required"));
                throw errors;
            }
            if (username.length() < 10) {
                errors.addError(new FieldError(loginModelStr, usernameStr, "Username must be at least 10 characters long"));
                throw errors;
            }
            if (username.length() > 60) {
                errors.addError(new FieldError(loginModelStr, usernameStr, "Username must be at most 60 characters long"));
                throw errors;
            }
            return baseUserRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        } else {
            String email = loginModel.getUsername();

            validateEmail(email, errors);

            if (email.length() < 10) {
                errors.addError(new FieldError(loginModelStr, emailStr, "Email must be at least 10 characters long"));
                throw errors;
            }
            if (email.length() > 120) {
                errors.addError(new FieldError(loginModelStr, emailStr, "Email must be at most 60 characters long"));
                throw errors;
            }
            return baseUserRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        }
    }

    private void validateEmail(String email, BindException errors) {
        Set<ConstraintViolation<EmailValidationModel>> violations = validator.validate(new EmailValidationModel(email));

        for (ConstraintViolation<EmailValidationModel> violation : violations) {
            errors.addError(new FieldError(
                    "baseUserModel",
                    "email",
                    violation.getMessage()
            ));
        }
    }

    private boolean isLocked(BaseUserEntity baseUserEntity) {
        LocalDateTime until = baseUserEntity.getLockedUntil();
        return until != null && until.isAfter(LocalDateTime.now());
    }

    private void applyBackoffIfNeeded(BaseUserEntity baseUserEntity) {
        int attempts = baseUserEntity.getFailedAttempts();

        // Empieza a ralentizar desde el 3er intento fallido
        if (attempts < 3) return;

        // 3->500ms, 4->1000ms, 5->2000ms, 6+->3000ms (cap)
        long delayMs = switch (attempts) {
            case 3 -> 500L;
            case 4 -> 1000L;
            case 5 -> 2000L;
            default -> 3000L;
        };

        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void resetFailures(BaseUserEntity baseUserEntity) {
        baseUserEntity.setFailedAttempts(0);
        baseUserEntity.setLockedUntil(null);
        baseUserEntity.setLastFailedAt(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(Long userId) {
        BaseUserEntity managed = baseUserRepository.findById(userId).orElse(null);
        if (managed == null) {
            return;
        }
        registerFailure(managed);
        baseUserRepository.save(managed);
    }

    private void registerFailure(BaseUserEntity baseUserEntity) {
        int next = baseUserEntity.getFailedAttempts() + 1;
        baseUserEntity.setFailedAttempts(next);
        baseUserEntity.setLastFailedAt(LocalDateTime.now());

        if (next >= MAX_FAILED_ATTEMPTS) {
            baseUserEntity.setLockedUntil(LocalDateTime.now().plusHours(LOCK_HOURS));
        }
    }

}
