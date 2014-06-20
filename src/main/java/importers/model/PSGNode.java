package importers.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public abstract class PSGNode {
 //Note: since all fields of this class and it's subclasses are final, there is no need for getter/setter capsulation

    /** the string denoting the type of a word (the POS) or the phrase/constituent, as it occurs in the original data*/
    public final String typeDesc;

    protected PSGNode(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public static class NonTerminal extends PSGNode {

        /** child nodes for this NonTerminal, in the left-to-right order as their occur in the parsed sentence */
        public final List<PSGNode> children = Lists.newLinkedList();

        public NonTerminal(String phraseTypeDesc) {
            super(phraseTypeDesc);
        }

        public String getPhraseTypeDesc() {
            return typeDesc;
        };

        public void addChildNode(PSGNode node) {
            children.add(node);
        }
    }

    public static class Terminal extends PSGNode {

        /** the word that this Terminal represents */
        public final String token;

        public Terminal(String posDesc, String token) {
            super(posDesc);
            this.token = token;
        }

        public String getPOSDesc() {
            return typeDesc;
        }
    }
}
