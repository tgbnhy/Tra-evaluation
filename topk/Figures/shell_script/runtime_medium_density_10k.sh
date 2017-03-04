source_file_dir="../raw_result/"

evaluation=runtime
density=medium
k=10

destination_file="../processed_result/runtime_medium_density_10k.txt"

for number_query_point in 2 4 6 8 10
do

file="${source_file_dir}/${evaluation}_${density}_density_${number_query_point}query_points_10k.txt"
IKNN_time=`grep "IKNN" $file | cut -d":" -f4`
GH_time=`grep GH $file | cut -d":" -f4`
SRA_time=`grep SRA $file | cut -d":" -f4`
SGRA_time=`grep SGRA $file | cut -d":" -f4`

echo "${number_query_point}${IKNN_time}${GH_time}${SRA_time}${SGRA_time}" >> $destination_file

done
echo $destination_file
