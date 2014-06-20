package config;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public enum Environment {

    DEVELOPMENT("development", "dev"),
    TEST("test", "test"),
    PRODUCTION("production", "prod");

    Environment(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public final String name;
    public final String shortName;


    public static final Environment DEFAULT_ENVIRONMENT = DEVELOPMENT;


    public static Environment byString(String str) {
        return package$.MODULE$.envByString(str);
    }

    public static Environment getActive() {
        return package$.MODULE$.activeEnvironment();
    }

    public static class UnrecognizedEnvironmentException extends RuntimeException {

        UnrecognizedEnvironmentException(String unmatchedEnvName) {
            super(String.format("No environment machtes '%s'", unmatchedEnvName));
        }
    }


    public static class AmbiguousEnvironmentException extends RuntimeException {

        public AmbiguousEnvironmentException(String... unmatchedEnvNames) {
            super(String.format("Found multiple non-identical enviroment requests: '%s'",
                    Joiner.on(", ").join(unmatchedEnvNames)));
        }

        public AmbiguousEnvironmentException(String ambiguousRequest) {
            super(String.format("Enviroment request: '%s'", ambiguousRequest));
        }
    }
}
