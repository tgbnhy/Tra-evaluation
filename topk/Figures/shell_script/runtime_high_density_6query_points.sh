source_file_dir="../raw_result"

evaluation=runtime
density=high
query_points=6

destination_file="../processed_result/runtime_high_density_6query_points.txt"

for k in 2 4 6 8 10 12 14 16
do

file="${source_file_dir}/${evaluation}_${density}_density_6query_points_${k}k.txt"
IKNN_time=`grep "IKNN" $file | cut -d":" -f4`
GH_time=`grep GH $file | cut -d":" -f4`
SRA_time=`grep SRA $file | cut -d":" -f4`
SGRA_time=`grep SGRA $file | cut -d":" -f4`

echo "${k}${IKNN_time}${GH_time}${SRA_time}${SGRA_time}" >> $destination_file

done
echo $destination_file
