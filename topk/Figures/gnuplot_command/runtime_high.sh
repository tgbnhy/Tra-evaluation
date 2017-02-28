source_file="../processed_result/runtime_high.txt"
eps_file="../runtime_high.eps"

cmd="

set terminal postscript eps enhanced color 32
set output \"${eps_file}\"

set tmargin 1
set rmargin 2
set lmargin 6
set bmargin 2

#set xrange [ 0: ]
set yrange [ 0: ]
set ytics font \"Helvetica-Bold, 24\"
set xtics offset 0,0.5 font \"Helvetica-Bold, 24\"

set ylabel offset 2.4,0 \"CPU Cost (ms)\" font \"Helvetica-Bold, 26\"
set xlabel offset 0,1.2 \"Number of Query Points\" font \"Helvetica-Bold, 26\"

set key left font ',20' spacing 1.5
set boxwidth 0.8
set size 1,1

plot '${source_file}' u 1:2 title \"IKNN\" w lp lt 1 lw 5 pt 1 ps 4 lc rgb \"black\",\
'${source_file}' u 1:3 title \"GH\" w lp lt 2 lw 5 pt 2 ps 4 lc rgb \"black\",\
'${source_file}' u 1:4 title \"SRA\" w lp lt 3 lw 5 pt 3 ps 4 lc rgb \"black\",\
'${source_file}' u 1:5 title \"SGRA\" w lp lt 4 lw 5 pt 4 ps 4 lc rgb \"black\"
"

echo "$cmd" | gnuplot
