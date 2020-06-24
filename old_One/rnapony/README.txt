kompilacja
c++ -std=c++11 -c StringTokenizer.cpp
c++ -std=c++11 cse_hairpin.cpp -o cse_hairpin StringTokenizer.o
c++ -std=c++11 cse_loop.cpp -o cse_loop StringTokenizer.o
c++ -std=c++11 cse_loop_open.cpp -o cse_loop_open StringTokenizer.o

Uruchomienie
./cse_hairpin hairpin.dot cse.txt 0
./cse_loop dinucl_steps.dot cse.txt 0
./cse_loop bulge.dot cse.txt 0
./cse_loop bulge1.dot cse.txt 0
./cse_loop ur15_spinka.dot cse.txt 0
./cse_loop ur4_L1.dot cse.txt 0
./cse_loop ur4_L1.dot cse.txt 1
./cse_loop ur4_L2.dot cse.txt 0
./cse_loop ur4_L2.dot cse.txt 1

oblicznie homologii
./cse_loop ur4_L1.dot cse.txt 0 > ur4_L1.out
./homology ur4_L1.out
Uwaga: oblicznie homologii nie jest poprawne jezeli damy insercje, gdyz nie wprowadzilem
jeszcze dopasowywnia sekwencji ani topologii

./cse_loop_open bp.dot cse.txt 0
uwaga: listuje wszystkie pary kanoniczne
./cse_loop_open bps2_4.dot cse.txt 0
./cse_loop_open bps3_4_10_20.dot cse.txt 0
uwaga: program cse_loop_open rozni sie od programu cse_loop tym, ze
ostatni warunek zamkniecia pętli nie musi byc spelniony.
Ten program jeszcze musi byc przetestowany.


