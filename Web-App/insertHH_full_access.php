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
$result = mysqli_query($con, $sql);

mysqli_close($con);
