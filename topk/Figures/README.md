workflow:
raw_result -> shell_script -> processed_result -> gnuplot_command -> eps format figures.

1.raw_result folder contains the output result from Eclipse project.
2.shell_script folder contains the shell scripts used to extract data from raw_result and store it to processed_result folder.
3.processed_result folder contains the data could be used to draw figures.
4.gnuplot_command folder contains all command used to draw figures based on the data in processed_result.
