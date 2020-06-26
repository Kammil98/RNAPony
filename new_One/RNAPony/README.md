# Działanie
##Przygotowanie
Należy wybrać plik, który chce się odpalić w klasie Main modułu Cse(podać w Stringu, jako parametr funkcji compute). <br />
Należy upewnić się, że w folderze RNAPony\Cse\src\main\resources znajdują się pliki, które chcemy odpalić dla Cse
oraz w folderze RNAPony\homology\src\main\resources pliki, które chcemy odpalić dla Homology (jeśli nie, to włożyć pliki i odpalić jeszcze raz obie komendy) <br />
##Kompilacja i uruchomienie
wejść do folderu głównego: <br />
cd .\RNAPony\new One\RNAPony <br />
odpalić poniższe komendy w folderze głównym(Uwaga! trzeba mieć zainstalowanego mavena wersja powyżej 3.5): <br />
mvn clean install<br />
java -jar .\Cse\target\Cse-1.0-SNAPSHOT-jar-with-dependencies.jar <br />



