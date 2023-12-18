package prime.prime.infrastructure.password.generator;

import org.passay.PasswordGenerator;

public final class RandomPasswordGenerator {
    static final PasswordGenerator passwordGenerator = new PasswordGenerator();

    public static String getRandomPassword() {
        return passwordGenerator.generatePassword(
                PasswordConstraint.LENGTH_RULE.getMinimumLength(),
                PasswordConstraint.UPPER_CASE,
                PasswordConstraint.LOWER_CASE,
                PasswordConstraint.DIGIT,
                PasswordConstraint.SPECIAL_CHAR);
    }
}
