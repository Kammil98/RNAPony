module Cse {
    requires Tokenizer;
    requires java.logging;
    requires static lombok;
    requires rnaponyutils;
    requires homology;
    opens cse;
    exports cse;
}