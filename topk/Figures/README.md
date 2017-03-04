workflow:
raw_result -> shell_script -> processed_result -> gnuplot_command -> eps format figures.


1.raw_result folder contains the output result from Eclipse project.
2.shell_script folder contains the shell scripts used to extract data from raw_result and store it to processed_result folder.
3.processed_result folder contains the data could be used to draw figures.
4.gnuplot_command folder contains all command used to draw figures based on the data in processed_result.



Example:

Figures/processed_result/runtime_high_density_10k.txt
----------------------------------------------------------------------\
query points | IKNN runtime | GH runtime | SRA runtime | SGRA runtime |
    2              47.48        103.76        51.30         45.69     |
    4             114.64         94.54        80.14         72.40     |
    6             237.44        103.35       100.04         86.44     |
    8             380.75        125.91       119.53         96.58     |
    10            530.47        152.60       134.07        105.88     |
----------------------------------------------------------------------/
