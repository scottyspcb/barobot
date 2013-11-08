<?





function h_to_rgb( $h, $c ){
    $hd = $h / 42;   // 42 == 252/6,  252 == H_MAX
    $hi = $hd % 6;   // gives 0-5
    $f = $h % 42; 
    $fs = $f * 6;
    switch( $hi ) {
        case 0:
            $r = 252;     $g = $fs;      $b = 0;
           break;
        case 1:
            $r = 252-$fs;  $g = 252;     $b = 0;
            break;
        case 2:
            $r = 0;       $g = 252;     $b = $fs;
            break;
        case 3:
            $r = 0;       $g = 252-$fs;  $b = 252;
            break;
        case 4:
            $r = $fs;      $g = 0;       $b = 252;
            break;
        case 5:
            $r = 252;     $g = 0;       $b = 252-$fs;
            break;
    }
}
?>

<table>
	<?
	for($i=0;$i<256;$i++){
		echo "<tr>";
			$a = h_to_rgb();
		for($j=0;$j<256;$j++){
			echo "<td>".  ."</td>";
		
		}
		echo "</tr>";
	}
	

</table>