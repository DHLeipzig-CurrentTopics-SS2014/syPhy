package importers;

import com.google.common.base.Optional;
import config.Locations;
import importers.model.PSGNode;
import importers.model.SyntaxCorpus;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Note: An instance of this class ensures that the sentence iterator will only by requested once.
 * Note: Importers are AutoClosable to ensure proper freeing of resources (e.g. open file handles) when
 *       the iterator will not be exhausted. (Use them in a try-with-resources statment).
 *
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class SyntaxCorpusImporter implements AutoCloseable {

    private Optional<Iterator<PSGNode>> iteratorPromise = Optional.absent();

    /** @return the corpus this importer is suitable for */
    public abstract SyntaxCorpus getCorpus();

    /**
     * @throws java.lang.IllegalStateException if this method is called more than once on the same instance
     * @return iterator for the root nodes of the sentences parsed from the corpus data
     */
    public Iterator<PSGNode> iterateSentences() {
        if(iteratorPromise.isPresent()) {
            throw new IllegalStateException("sentence iterator can only be requested once");
        } else {
            iteratorPromise = Optional.of(createSentenceIterator());
            return iteratorPromise.get();
        }
    };

    /** internal template method for the actual iterator creation */
    protected abstract Iterator<PSGNode> createSentenceIterator();

    protected Path dataRoot () {
        Path resolved = Locations.corpusDataRoot().resolve(getCorpus().dataSubPath);
        try {
            return resolved.toRealPath();
        } catch (IOException ioe) {
            throw new RuntimeException("corpus data cannot be accessed at " + resolved, ioe);
        }
    }
}
