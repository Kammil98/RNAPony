module Cse {
    requires Tokenizer;
    requires java.logging;
    requires static lombok;
    opens cse;
    exports cse;
    exports models;
}