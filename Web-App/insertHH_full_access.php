<?php

      $hostname = "localhost";
      $username = "ted";
      $dbname = "";
      $con = mysqli_connect($hostname, $username, '', $dbname);


$q = $_GET['q'];

$parts = explode(',', $q);


if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
    echo 'stupidity is defined';
}
$date = date("Y-m-d H:i:s");
$ipAddress = $_SERVER['REMOTE_ADDR'];
if (array_key_exists('HTTP_X_FORWARDED_FOR', $_SERVER)) {
    $ipAddress = array_pop(explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']));
}
$fb_id = $parts[39];
$handNum = (int) $parts[29];

$sql="SELECT count(1) as ct FROM `hh_full_access` WHERE ipaddress = '" . $ipAddress . "' and gamehandnum = 1";
$result = mysqli_query($con, $sql);
$row = mysqli_fetch_array($result);

$ipSessions = $row['ct'];

if ($handNum == 1) {
    $ipSessions = $ipSessions + 1;
}





/*echo 'fb_id is ' . $fb_id . ' type is ' . gettype($fb_id);
//if no facebook id increment session number if its the first hand
if($fb_id == '0') { //if fb_id is 0 and first hand
    //$sql="SELECT max(Session_Number) as Session_Number FROM `hh2` WHERE ipaddress = '" . $ipAddress . "'";
    $sql="SELECT ifnull(max(Session_Number),0) as Session_Number FROM `hh2` WHERE ipaddress = '5'";
    //echo $sql;
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_array($result);
    $sn = $row['Session_Number'];
    echo 'session number is ' . $sn;
    if(((int) $parts[29]) == 1){
        $sn = $sn + 1;
    }
    if($sn == 0){
        $sn = 1;
    }
}

//get session number and increment it if its the first hand if there is a fb id
if($fb_id != '0') { //if there is a fb_id  and first hand
    $sql="SELECT ifnull(max(Session_Number),0) as Session_Number FROM `hh2` WHERE FB_ID = " . $fb_id;
    $result = mysqli_query($con,$sql);
    $row = mysqli_fetch_array($result);
    $sn = $row['Session_Number'];
    if($parts[29] == 1){
        $sn = $sn + 1;
    }

    if($sn == 0){
        $sn = 1;
    }
}

*/

$sql = "INSERT INTO hh_full_access(Score, C1, C2, B1, B2, B3, B4, B5, P1, P2, Button, Percentile, 
		ShowDown, Date, IPaddress, BigBet, CompRaise_PF, PlayerRaise_PF, Last_Raise_PF, Bet_PF,
		CompRaise_Flop, PlayerRaise_Flop, Last_Raise_Flop, Bet_Flop, CompRaise_Turn, PlayerRaise_Turn, Last_Raise_Turn, Bet_Turn,
		CompRaise_River, PlayerRaise_River, Last_Raise_River, Bet_River, GameHandNum, Comp_PF_Value, Player_PF_Value,
		Comp_Flop_Perc, Player_Flop_Perc, Comp_Turn_Perc, Player_Turn_Perc, Comp_River_Perc, Player_River_Perc,
		FB_Name, FB_ID, Start_Chips, Session_Number_IP)		 
		VALUES (" . $parts[0];
for ($i = 1; $i < 10; $i++) {
    $sql = $sql . ", '" . $parts[$i] . "'";
}
$sql = $sql . ",'" . $parts[10] . "'";
$sql = $sql . "," . $parts[11];
$sql = $sql . "," . $parts[12];
$sql = $sql . ",'" . $date . "'";
$sql = $sql . ",'" . $ipAddress . "',20";

for ($i = 13; $i < 41; $i++) {
    $sql = $sql . ", " . $parts[$i];
}

$sql = $sql . ", " . $ipSessions;
$sql = $sql . ")";
//echo $sql;

$result = mysqli_query($con, $sql);
//echo '38 is ' . $parts[37];
//echo ' howdy fellas';

#echo $result;
#echo $sql;

mysqli_close($con);
