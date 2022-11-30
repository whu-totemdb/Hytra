
set term postscript enhanced eps 32
set output "sim_data_k_syd.eps"
set style data histogram

set tmargin 1
set rmargin 1
set lmargin 5
set bmargin 3

set xrange [-0.5:4.5]
set yrange [1:10000]  noreverse nowriteback

# set title "Sydney" font 'Calibri, 30' offset 0,-0.7,0

#set xtics font 'Calibri-Bold, 35' scale 0 offset 0, 0 ("Argoverse" 2.5)
set ytics font 'Calibri, 26'

set xtics font 'Calibri, 26' ( "10"  0, "20" 1, "30" 2, "40" 3, "50" 4)

set xlabel offset 0, 0.5 "{/Italic: k}" font  'Calibri-Bold, 32'

#set xlabel "topK"  font  'Calibri-Bold,26' 
set ylabel offset 1.5, 0 "Time Cost (ms)"   font  'Calibri-Bold,33' 
set logscale y
set format y "10^{%L}"
set key top left horizontal font  'Calibri-Bold,30' spacing 1 maxrow 1

set style data histogram
set style fill pattern 3 border -1

set datafile missing '-'

plot 'sim_data_k_syd.dat' using 2 title columnheader (1),  'sim_data_k_syd.dat' using 3  title columnheader (2), 'sim_data_k_syd.dat' using 4 title columnheader (3)
set output