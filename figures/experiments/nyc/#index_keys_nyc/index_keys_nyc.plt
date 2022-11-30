
set term postscript enhanced eps 32
set output "index_keys_nyc.eps"
set style data histogram

set tmargin 1
set rmargin 1
set lmargin 5
set bmargin 3

set xrange [-0.5:4.5]
set yrange [10:10000000]  noreverse nowriteback

# set title "NYC" font 'Calibri, 30' offset 0,-0.7,0

#set xtics font 'Calibri-Bold, 35' scale 0 offset 0, 0 ("Argoverse" 2.5)
set ytics font 'Calibri, 28'

set xtics font 'Calibri, 30' ( "4"  0, "5" 1, "6" 2, "7" 3, "8" 4)

set xlabel offset 0, 0.5 "Resolution" font  'Calibri-Bold, 32'

#set xlabel "topK"  font  'Calibri-Bold,26' 
set ylabel offset 1.4, 0 "# of Keys"   font  'Calibri-Bold,30' 
set logscale y
set format y "10^{%L}"
set key top left horizontal font  'Calibri-Bold,30' spacing 1 maxrow 1

set style data histogram
set style fill pattern 3 border -1

set datafile missing '-'

plot 'index_keys_nyc.dat' using 2 title columnheader (1),  'index_keys_nyc.dat' using 3  title columnheader (2)
set output