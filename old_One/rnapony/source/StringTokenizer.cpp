#include <string>
#include <iostream>
#include <vector>
#include "StringTokenizer.h"
StringTokenizer::StringTokenizer(const string& s, const char* delim = NULL):
     str_(s), count_(-1), begin_(0), end_(0) {
     if(!delim) delim_ = " \f\n\r\t\v";
     else delim_ = delim;
     begin_ = str_.find_first_not_of(delim_);
     end_ = str_.find_first_of(delim_, begin_);
                                             }
size_t StringTokenizer::countTokens(){
     if(count_ >= 0) return(count_);
     string::size_type n = 0;
     string::size_type i = 0;
     for(;;){
       if((i=str_.find_first_not_of(delim_, i))==string::npos) break;
       i = str_.find_first_of(delim_, i+1);
       n++;
       if(i==string::npos) break;
            }
      return(count_=n);
                        }
bool StringTokenizer::hasMoreTokens(){return(begin_ != end_);}
void StringTokenizer::nextToken(string& s){
       if(begin_ != string::npos && end_ != string::npos){
          s=str_.substr(begin_, end_-begin_);
          begin_ = str_.find_first_not_of(delim_, end_);
          end_ = str_.find_first_of(delim_, begin_);
                                                         }
       else if (begin_ != string::npos && end_ == string::npos){
                s = str_.substr(begin_, str_.length()-begin_);
                begin_ = str_.find_first_not_of(delim_, end_);
                                                               }

                                          }
