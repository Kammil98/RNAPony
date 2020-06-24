#include <iostream>
#include <string>
#include <sstream>
#include <vector>
using namespace std;

/*
template<typename T>
void split(const basic_string<T>& s, T c, vector<basic_string<T> >& v){
  basic_string<T>::size_type i = 0;
  basic_string<T>::size_type j = s.find(c);
  while(j != basic_string<T>::npos){
     v.push_back(s.substr(i, j-1));
     i = ++j;
     j = s.find(c,j);
     if(j == basic_string<T>::npos) v.push_back(s.substr(i, s.length()));
                                   }
}
*/

template<typename T>
T strToNum(const string& str){
 stringstream ss(str);
 T tmp;
 ss >> tmp;
 if(ss.fail()){
  string s = "Nie mozna sformatowac ";
  s+= str;
  s+= " na liczbe!";
  throw(s);
              }
 return (tmp);
}
class StringTokenizer{
public:
 StringTokenizer(const string& s, const char* delim );
 size_t countTokens();
 bool hasMoreTokens();
 void nextToken(string& s);
private:
  StringTokenizer(){};
  string delim_;
  string str_;
  int count_;
  int begin_;
  int end_;
                     };
