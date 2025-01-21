package se.fulkopinglibrary.fulkopinglibrary.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class User {
    private final int userId;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String name;
    private final String email;
    private final int failedAttempts;
    private final LocalDateTime lockoutUntil;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final boolean isDeleted;
    private final Set<String> roles;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]{4,20}$");

    public static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public User(int userId, String username, String passwordHash, String salt, 
                String name, String email, int failedAttempts, LocalDateTime lockoutUntil,
                LocalDateTime createdAt, LocalDateTime updatedAt, boolean isDeleted,
                Set<String> roles) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 4-20 alphanumeric characters");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }
        if (salt == null || salt.isBlank()) {
            throw new IllegalArgumentException("Salt cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (failedAttempts < 0) {
            throw new IllegalArgumentException("Failed attempts cannot be negative");
        }
        if (roles == null) {
            throw new IllegalArgumentException("Roles cannot be null");
        }

        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.name = name;
        this.email = email;
        this.failedAttempts = failedAttempts;
        this.lockoutUntil = lockoutUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isDeleted = isDeleted;
        this.roles = new HashSet<>(roles);
    }

    public User(int userId, String username, String name, String email) {
        this(userId, username, "", "", name, email, 0, null, 
             LocalDateTime.now(), LocalDateTime.now(), false, Set.of());
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLockoutUntil() {
        return lockoutUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }

    public boolean isLockedOut() {
        return lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now());
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isActive() {
        return !isDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId && 
               username.equals(user.username) &&
               email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", failedAttempts=" + failedAttempts +
                ", lockoutUntil=" + lockoutUntil +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isDeleted=" + isDeleted +
                ", roles=" + roles +
                '}';
    }

    public static class Builder {
        private int userId;
        private String username;
        private String passwordHash = "";
        private String salt = "";
        private String name;
        private String email;
        private int failedAttempts = 0;
        private LocalDateTime lockoutUntil;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private boolean isDeleted = false;
        private Set<String> roles = new HashSet<>();

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Builder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder failedAttempts(int failedAttempts) {
            this.failedAttempts = failedAttempts;
            return this;
        }

        public Builder lockoutUntil(LocalDateTime lockoutUntil) {
            this.lockoutUntil = lockoutUntil;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder isDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = new HashSet<>(roles);
            return this;
        }

        public Builder addRole(String role) {
            this.roles.add(role);
            return this;
        }

        public User build() {
            return new User(userId, username, passwordHash, salt, name, email,
                          failedAttempts, lockoutUntil, createdAt, updatedAt,
                          isDeleted, roles);
        }
    }
}
