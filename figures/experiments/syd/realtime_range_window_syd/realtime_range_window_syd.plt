
set term postscript enhanced eps 32
set output "realtime_range_window_syd.eps"
set style data histogram

set tmargin 1
set rmargin 1
set lmargin 5
set bmargin 3

set xrange [-0.5:4.5]
set yrange [1:200]  noreverse nowriteback

# set title "Sydney" font 'Calibri, 30' offset 0,-0.7,0

#set xtics font 'Calibri-Bold, 35' scale 0 offset 0, 0 ("Argoverse" 2.5)
set ytics font 'Calibri, 26'

set xtics font 'Calibri, 26' ( "1{/Symbol \264}1"  0, "2{/Symbol \264}2" 1, "3{/Symbol \264}3" 2, "4{/Symbol \264}4" 3, "5{/Symbol \264}5" 4)

set xlabel offset 0, 0.5 "Spatial Range (km^2)" font  'Calibri-Bold, 32'

#set xlabel "topK"  font  'Calibri-Bold,26' 
set ylabel offset 1.5, 0 "Time Cost (ms)"   font  'Calibri-Bold,33' 

set logscale y
set format y "10^{%L}"

set key top left horizontal font  'Calibri-Bold,30' spacing 1 maxrow 1

set style data histogram
set style fill pattern 3 border -1

set datafile missing '-'

plot 'realtime_range_window_syd.dat' using 2 title columnheader (1),  'realtime_range_window_syd.dat' using 3  title columnheader (2)
set output