#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import ${package}.domain.entity.UserEntity;
import ${package}.domain.repository.UserRepository;
import ${package}.validation.EmailValidationModel;
import ${package}.web.model.LoginModel;
import ${package}.web.model.RegisterUserModel;
import ${package}.web.model.TokenModel;
import ${package}.web.model.UserModel;
import io.github.luicit.luisprojectscore.config.CoreProperties;
import io.github.luicit.luisprojectscore.converter.GenericConverter;
import io.github.luicit.luisprojectscore.security.JwtUtil;
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

    private final GenericConverter<UserEntity, UserModel> userConverter =
            new GenericConverter<>(UserEntity::new, UserModel::new);

    private final JwtUtil jwtUtil;
    private final Validator validator;
    private final CoreProperties coreProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final AuthService self;

    protected String usernameStr = "username";
    protected String emailStr = "email";

    public AuthService(JwtUtil jwtUtil,
                       Validator validator,
                       CoreProperties coreProperties,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       @Lazy AuthService self) {
        this.jwtUtil = jwtUtil;
        this.validator = validator;
        this.coreProperties = coreProperties;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.self = self;
    }

    @Transactional
    public UserModel registerUser(RegisterUserModel registerUserModel) throws BindException {

        String userModelStr = "userModel";
        BindException errors = new BindException(registerUserModel, userModelStr);

        if (usernameStr.equalsIgnoreCase(coreProperties.getSecurity().getLoginIdentifier())) {
            String username = registerUserModel.getUsername();
            if (username == null || username.isBlank()) {
                errors.addError(new FieldError(userModelStr, usernameStr, "Username is required"));
                throw errors;
            } else if (userRepository.existsByUsernameAndDeletedAtIsNull(username.toLowerCase())) {
                errors.addError(new FieldError(userModelStr, usernameStr, "Username already exists"));
                throw errors;
            }
        } else {
            String email = registerUserModel.getEmail();
            if (email == null || email.isBlank()) {
                errors.addError(new FieldError(userModelStr, emailStr, "Email is required"));
                throw errors;
            } else if (userRepository.existsByEmailAndDeletedAtIsNull(email.toLowerCase())) {
                errors.addError(new FieldError(userModelStr, emailStr, "Email already exists"));
                throw errors;
            }
        }

        String encryptedPassword = passwordEncoder.encode(registerUserModel.getPassword());

        UserEntity newUser = getNewUserEntity(registerUserModel, encryptedPassword);

        userRepository.save(newUser);
        LOG.debug("Created Information for User: {}", newUser);

        return userConverter.aModelo(newUser);
    }

    private static @NonNull UserEntity getNewUserEntity(RegisterUserModel registerUserModel, String encryptedPassword) {
        UserEntity newUser = new UserEntity();
        newUser.setId(0L);
        newUser.setName(registerUserModel.getName());
        newUser.setUsername(registerUserModel.getUsername());
        newUser.setEmail(registerUserModel.getEmail());
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

        UserEntity userEntity = validateLoginIdentifier(loginModel);

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
                userRepository.save(userEntity);
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

    private UserEntity validateLoginIdentifier(LoginModel loginModel) throws BindException {
        String loginModelStr = "loginModel";
        BindException errors = new BindException(loginModel, loginModelStr);

        if (usernameStr.equalsIgnoreCase(coreProperties.getSecurity().getLoginIdentifier())) {
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
            return userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
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
            return userRepository.findByEmailAndDeletedAtIsNull(email).orElse(null);
        }
    }

    private void validateEmail(String email, BindException errors) {
        Set<ConstraintViolation<EmailValidationModel>> violations = validator.validate(new EmailValidationModel(email));

        for (ConstraintViolation<EmailValidationModel> violation : violations) {
            errors.addError(new FieldError(
                    "userModel",
                    "email",
                    violation.getMessage()
            ));
        }
    }

    private boolean isLocked(UserEntity userEntity) {
        LocalDateTime until = userEntity.getLockedUntil();
        return until != null && until.isAfter(LocalDateTime.now());
    }

    private void applyBackoffIfNeeded(UserEntity userEntity) {
        int attempts = userEntity.getFailedAttempts();

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

    private void resetFailures(UserEntity userEntity) {
        userEntity.setFailedAttempts(0);
        userEntity.setLockedUntil(null);
        userEntity.setLastFailedAt(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(Long userId) {
        UserEntity managed = userRepository.findById(userId).orElse(null);
        if (managed == null) {
            return;
        }
        registerFailure(managed);
        userRepository.save(managed);
    }

    private void registerFailure(UserEntity userEntity) {
        int next = userEntity.getFailedAttempts() + 1;
        userEntity.setFailedAttempts(next);
        userEntity.setLastFailedAt(LocalDateTime.now());

        if (next >= MAX_FAILED_ATTEMPTS) {
            userEntity.setLockedUntil(LocalDateTime.now().plusHours(LOCK_HOURS));
        }
    }

}
