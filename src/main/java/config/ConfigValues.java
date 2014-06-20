package config;

/**
 * A central place for hard-coded, but possibly environment-dependent configuration values.
 *
 * Created by Markus Ackermann.
 * No rights reserved.
 */
//Note: This class will later probably be replaced by a *.properties file
public class ConfigValues {

    /** system-dependent string giving the location of the root of the corpus data directory (relative pahts will be
     * resolved against the project root directory.*/
    public static final String CORPUS_DATA_DIR = "../corpora";
}
