module Cse {
    requires Tokenizer;
    requires java.logging;
    requires static lombok;
    requires rnaponyutils;
    requires homology;
    exports cse;
    opens cse;
    opens csemodels;
}