module Cse {
    requires Tokenizer;
    requires java.logging;
    requires static lombok;
    requires rnaponyutils;
    requires homology;
    requires jcommander;
    exports cse;
    exports csemodels.parameters.converters to jcommander;
    opens cse;
    opens csemodels;
}