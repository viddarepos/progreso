package prime.prime.infrastructure.password.generator;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;

public class PasswordConstraint {

    static CharacterRule specialCharacterRule = new CharacterRule(new CharacterData() {
        @Override
        public String getErrorCode() {
            return "Wrong characters";
        }

        @Override
        public String getCharacters() {
            return "~`!@#$%^&*()_-+={[}]|:;\"'<,>.?/";
        }
    }, 1);

    public static final LengthRule LENGTH_RULE = new LengthRule(8, 100);
    public static final CharacterRule UPPER_CASE = new CharacterRule(EnglishCharacterData.UpperCase, 1);
    public static final CharacterRule LOWER_CASE = new CharacterRule(EnglishCharacterData.LowerCase, 1);
    public static final CharacterRule DIGIT = new CharacterRule(EnglishCharacterData.Digit, 1);
    public static final CharacterRule SPECIAL_CHAR = specialCharacterRule;
}
