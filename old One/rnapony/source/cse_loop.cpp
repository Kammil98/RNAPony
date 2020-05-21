#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <iomanip>
#include <map>
#include <cmath>
#include <sstream>
#include <functional>
#include "StringTokenizer.h"
using namespace std;
const char SEPARATOR = ';';
const string base ="acgu";
const int MAX_ORDER_LOOP=20;
const string CHARS_BP1 = "([{<ABCDEFGHIJK"; const string  CHARS_BP2 = ")]}>abcdefghijk";
void read_mp_seq(const string& filename, string& nameseq, string& seq, string& top);
void read_database(const string& filename,vector<string>& vb_pdb, vector<string>& vb_chain, vector<float>& vb_resol,\
	       	vector<string>& vb_seq, vector<string>& vb_top, vector<string>& vbs_bp, vector<int>& vb_order);
void show_database(const vector<string>& vb_pdb, const vector<string>& vb_chain, const vector<float>& vb_resol,\
	       	const vector<string>& vb_seq, const vector<string>& vb_top, const vector<string>& vbs_bp, const vector<int>& vb_order);
void create_vect(const string& source, vector<string>& target);
void create_vectint(const string& source, vector<int>& target);
bool is_ok(const vector<pair<int,int>>& n, const int& x1, const int& x2);

// ------------------------------------------------------MAIN--------------------------------
int main(int argc, char *argv[]){
bool tmp_bool=true;int j=0,limit=0,start=0,tmpint=0,x1=0,x2=0,li=0,nins=0,i0=0,i1=0,i2=0,i3=0;
string filename_mp_seq="",filename_database="",bseq="",bdot="",bbp="",tmpstr="",seq="",top="",nameseq="";
vector<int>vb_order,steps,steps_origin,bbps;vector<bool>direct;vector<float>vb_resol;vector<pair<int,int>> n;
vector<string>vb_pdb,vb_chain,vb_seq,vb_top,vbs_bp,seqs,tops;
if(argc!=4){cout << "Usage : a.out <filename_dot> <filename_database> insert"<< endl; return -1;}
filename_mp_seq=argv[1];filename_database=argv[2];nins=atoi(argv[3]);
read_mp_seq(filename_mp_seq, nameseq, seq, top);cout << nameseq << " " << seq << " " << top << endl;
read_database(filename_database,vb_pdb, vb_chain, vb_resol, vb_seq, vb_top, vbs_bp, vb_order);
create_vect(seq,seqs);create_vect(top,tops);create_vectint(bbp,bbps);
for(int i=0;i<seqs.size();i++){steps_origin.push_back(seqs[i].length()-1);steps.push_back(seqs[i].length()-1);
	if(base.find(seqs[i].substr(0,1))==string::npos){direct.push_back(true);}else{direct.push_back(false);/*steps_origin[i]=-steps_origin[i];*/}
                              }
//cout << "nins= " << nins << " steps_origin.size()= " << steps_origin.size() << " nins*steps_origin.size())= " << nins*steps_origin.size() << endl;
for(i0=(steps_origin.size()-1);i0<((nins+1)*steps_origin.size());i0++){
i1=i0/steps_origin.size();cout << "INSERT= " << i1 << endl;i2=i0%steps_origin.size();
  if(direct[i2]){steps[i2]=steps_origin[i2]+i1;}else{steps[i2]=-(steps_origin[i2]+i1);}
//for(int r=0; r<seqs.size();r++){
  x1=0;x2=0;
  for(int a=0; a<vb_pdb.size(); a++){create_vectint(vbs_bp[a],bbps);limit=bbps.size();
    for(int i=0;i<20;i++){bbps.push_back(0);}
    if(steps[0]>0){start=0;limit-=steps[0];}else{start=-steps[0];}
    for(int i=start;i<limit;i++){
      if((bbps[i]/*>*/!=0)&&(bbps[i+steps[0]]!=0)){if(n.size()>0){n.clear();}
        j=0;x1=i;x2=x1+steps[j];n.push_back({x1,x2});
        for(j=1;j<steps.size();j++){x1=x2+bbps[x2];
          if(x1==0) break;
          x2=x1+steps[j];
          if(x2>=bbps.size()) break;
          if(not is_ok(n,x1,x2)) {break;}
          n.push_back({x1,x2});
                                   }
      if(j==steps.size()){
	if(i==bbps[x2]+x2){li++;
cout << "NR "<< setw(5) << li << /*" rot "*/ " ins " << /*r*/i1 <<" "<< vb_pdb[a]<<" "<<vb_chain[a]<<" "<<setw(5) <<setprecision(5)<<vb_resol[a];
for(int k=0;k<n.size();k++){
if(steps[k]>0){cout << " " << n[k].first+1 << " - " << n[k].second+1 << " " << vb_seq[a].substr(n[k].first,steps[k]+1) << " " << vb_top[a].substr(n[k].first,steps[k]+1);}else{
   cout << " " << n[k].second+1 << " - " << n[k].first+1 << " " << vb_seq[a].substr(n[k].second,-(steps[k]-1)) << " " << vb_top[a].substr(n[k].second,-(steps[k]-1));}
                           }
cout << endl;
	                  }
	                 } 
	                                    }
                                       }
                                    } // for a
//cout << "TUTAJ\n";
//                                } //for r
steps[i2]=steps_origin[i2];
                                               } //for i0
return 0;}
// ---------------------------------------------------END MAIN -------------------------------------
void read_mp_seq(const string& filename, string& nameseq, string& seq, string& top){
ifstream fin(filename.c_str());int li;string line;
while(!fin.eof()){
  getline(fin, line, '\n');li++;
  if(li==1){nameseq=line.substr(1);}
  if(li==2){seq=line;}
  if(li==3){top=line;}
}
fin.close();
}

void read_database(const string& filename,vector<string>& vb_pdb, vector<string>& vb_chain, vector<float>& vb_resol,\
 vector<string>& vb_seq, vector<string>& vb_top, vector<string>& vbs_bp, vector<int>& vb_order){
ifstream fin(filename.c_str());string tmp,line;
while(!fin.eof()){
  getline(fin, line, '\n');StringTokenizer st(line," ");
  if(st.countTokens()==7){
    st.nextToken(tmp);vb_pdb.push_back(tmp);st.nextToken(tmp);vb_chain.push_back(tmp);
    st.nextToken(tmp);vb_resol.push_back(strToNum<float>(tmp));st.nextToken(tmp);vb_seq.push_back(tmp);
    st.nextToken(tmp);vb_top.push_back(tmp);st.nextToken(tmp);vbs_bp.push_back(tmp);
    st.nextToken(tmp);vb_order.push_back(strToNum<int>(tmp));
                          }};fin.close();}

void show_database(const vector<string>& vb_pdb, const vector<string>& vb_chain, const vector<float>& vb_resol,\
                const vector<string>& vb_seq, const vector<string>& vb_top, const vector<string>& vbs_bp, const vector<int>& vb_order){
for(int i=0;i<vb_pdb.size();i++){
	cout << setw(5) << i+1 << endl;
	cout << "ID PDB: " << vb_pdb[i] << "   CHAINS: " << vb_chain[i] << "   ORDER: " << vb_order[i] << "   RESOLUTION: " << vb_resol[i] << endl;
	cout << "SEQUENCE: " << vb_seq[i] << endl;
	cout << "TOPOLOGY: " << vb_top[i] << endl;
	cout << "BPSEQ:    " << vbs_bp[i] << endl << endl;
	                        }}

void create_vect(const string& source, vector<string>& target){
if(target.size()!=string::npos){target.clear();}
string::size_type ii=0,jj = source.find(SEPARATOR);
while(jj!=string::npos){target.push_back(source.substr(ii,jj-ii));ii=++jj;jj=source.find(SEPARATOR,jj);
if(jj==string::npos){target.push_back(source.substr(ii,source.length()));}
}}

void create_vectint(const string& source, vector<int>& target){
if(target.size()!=string::npos){target.clear();}
string::size_type ii=0,jj = source.find(SEPARATOR);
while(jj!=string::npos){target.push_back(strToNum<int>(source.substr(ii,jj-ii)));ii=++jj;jj=source.find(SEPARATOR,jj);
if(jj==string::npos){target.push_back(strToNum<int>(source.substr(ii,source.length())));}
}}

bool is_ok(const vector<pair<int,int>>& n, const int& x1, const int& x2){
for(int i=0;i<n.size();i++){
  if((x1>=n[i].first)&&(x1<=n[i].second)){return false;}
  if((x2>=n[i].first)&&(x2<=n[i].second)){return false;}
  if((x1<n[i].first)&&(x2>n[i].second)){return false;}
	                   }
return true;
}

/*
bool is_ok(const vector<pair<int,int>>& n, const int& x1, const int& x2){
for(int i=0;i<n.size();i++){
  if((x1>=n[i].first)&&(x1<=n[i].second)){return false;}
  if((x2>=n[i].first)&&(x2<=n[i].second)){return false;}
  if(x1<x2){
  if((x1<n[i].first)&&(x2>n[i].second)){return false;}
           }else{
  if((x2<n[i].first)&&(x1>n[i].second)){return false;}
                }
	                   }
return true;
}
*/

void create_pattern_shift(const string& seq,const string& top, const vector<int>& vlength_seq, const vector<int>& vbp1_pos, const vector<int>& vbp2_pos, const vector<int>& vbp_order, string& seqs_new, string& tops_new){
char c1,c2;int n;const char c=SEPARATOR;
string tmp(top);
seqs_new="";tops_new="";
if(vlength_seq.size()==0){seqs_new=seq;tops_new=top;}else{
for(vector<int>::size_type i=0; i<vbp1_pos.size(); i++){
 if((vbp1_pos[i]<vlength_seq[0])&&(vbp2_pos[i]>=vlength_seq[0])){c1=CHARS_BP2[vbp_order[i]];c2=CHARS_BP1[vbp_order[i]];
  tmp[vbp1_pos[i]]=c1;tmp[vbp2_pos[i]]=c2;               }
                                                       }
  n=vlength_seq[0];tops_new+=tmp.substr(n,vlength_seq[1]);/*tops_new+=c;*/
   seqs_new+=seq.substr(n,vlength_seq[1]);/*seqs_new+=c;*/
  for(vector<int>::size_type i=1;i<(vlength_seq.size()-1);i++){
      n+=vlength_seq[i];tops_new+=tmp.substr(n,vlength_seq[i+1]);/* tops_new+=c;*/
                        seqs_new+=seq.substr(n,vlength_seq[i+1]);/*seqs_new+=c;*/}
  tops_new+=tmp.substr(0,vlength_seq[0]);seqs_new+=seq.substr(0,vlength_seq[0]);
}}

