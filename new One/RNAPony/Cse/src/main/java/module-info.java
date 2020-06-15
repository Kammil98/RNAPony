module Cse {
    requires Tokenizer;
    requires java.logging;
    //requires transitive lombok;
    opens cse;
    exports cse;
    exports models;
}