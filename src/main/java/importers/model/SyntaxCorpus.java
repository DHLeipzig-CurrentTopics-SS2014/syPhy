package importers.model;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public enum SyntaxCorpus {

    FRENCH_TB ("French Treebank", Paths.get("french_tb", "data", "FrenchTreebank")),
    YH_PCOE   ("York Helsinki Parsed Corpus of Old English", Paths.get("york_helsinki", "data", "ycoe"));
    //TODO: add the rest of the corpus definitions


    /** longer, more descriptive/official name of the corpus */
    public final String longName;
    /** relative path to be resolved against the location of the corpus data directory to obtain the corpus data input
     *  file(s) */
    public final Path dataSubPath;

    SyntaxCorpus(String longName, Path dataSubPath) {
        this.longName = longName;
        this.dataSubPath = dataSubPath;
    };
}
