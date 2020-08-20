<?php

$hostname = "localhost";
$username = "ted";
$dbname = "";
$con = mysqli_connect($hostname, $username, '', $dbname);

if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
}

$Date = date("Y-m-d H:i:s");
$IPaddress = $_SERVER['REMOTE_ADDR'];
if (array_key_exists('HTTP_X_FORWARDED_FOR', $_SERVER)) {
    $IPaddress = array_pop(explode(',', $_SERVER['HTTP_X_FORWARDED_FOR']));
}

$Guid = $_REQUEST['Guid'];
$Score = (int) $_REQUEST['Score'];
$C1 = $_REQUEST['C1'];
$C2 = $_REQUEST['C2'];
$B1 = $_REQUEST['B1'];
$B2 = $_REQUEST['B2'];
$B3 = $_REQUEST['B3'];
$B4 = $_REQUEST['B4'];
$B5 = $_REQUEST['B5'];
$P1 = $_REQUEST['P1'];
$P2 = $_REQUEST['P2'];
$Button = $_REQUEST['Button'];
$Percentile =$_REQUEST['Percentile'];
$ShowDown=$_REQUEST['ShowDown'];
$BigBet=$_REQUEST['BigBet'];
$CompRaise_PF=$_REQUEST['CompRaise_PF'];
$PlayerRaise_PF=$_REQUEST['PlayerRaise_PF'];
$Last_Raise_PF=$_REQUEST['Last_Raise_PF'];
$Bet_PF=$_REQUEST['Bet_PF'];
$CompRaise_Flop=$_REQUEST['CompRaise_Flop'];
$PlayerRaise_Flop=$_REQUEST['PlayerRaise_Flop'];
$Last_Raise_Flop=$_REQUEST['Last_Raise_Flop'];
$Bet_Flop=$_REQUEST['Bet_Flop'];
$CompRaise_Turn=$_REQUEST['CompRaise_Turn'];
$PlayerRaise_Turn=$_REQUEST['PlayerRaise_Turn'];
$Last_Raise_Turn=$_REQUEST['Last_Raise_Turn'];
$Bet_Turn=$_REQUEST['Bet_Turn'];
$CompRaise_River=$_REQUEST['CompRaise_River'];
$PlayerRaise_River=$_REQUEST['PlayerRaise_River'];
$Last_Raise_River=$_REQUEST['Last_Raise_River'];
$Bet_River=$_REQUEST['Bet_River'];
$GameHandNum=$_REQUEST['GameHandNum'];
$Comp_PF_Value=$_REQUEST['Comp_PF_Value'];
$Player_PF_Value=$_REQUEST['Player_PF_Value'];
$Comp_Flop_Perc=$_REQUEST['Comp_Flop_Perc'];
$Player_Flop_Perc=$_REQUEST['Player_Flop_Perc'];
$Comp_Turn_Perc=$_REQUEST['Comp_Turn_Perc'];
$Player_Turn_Perc=$_REQUEST['Player_Turn_Perc'];
$Comp_River_Perc=$_REQUEST['Comp_River_Perc'];
$Player_River_Perc=$_REQUEST['Player_River_Perc'];
$Round_Completed = $_REQUEST['Round_Completed'];
$Stacked = $_REQUEST['Stacked'];
$FB_Name=$_REQUEST['FB_Name'];
$FB_ID=$_REQUEST['FB_ID'];
$Start_Chips=$_REQUEST['Start_Chips'];
$Email=$_REQUEST['Email'];

$sql = "INSERT INTO hh_android (Guid, Score, C1, C2, B1, B2, B3, B4, B5, P1, P2, Button, 
	Percentile, ShowDown, Date, IPaddress, BigBet, CompRaise_PF, PlayerRaise_PF,
	Last_Raise_PF, Bet_PF, CompRaise_Flop, PlayerRaise_Flop, Last_Raise_Flop, Bet_Flop,
	CompRaise_Turn, PlayerRaise_Turn, Last_Raise_Turn, Bet_Turn, CompRaise_River,
	PlayerRaise_River, Last_Raise_River, Bet_River, GameHandNum, Comp_PF_Value,
	Player_PF_Value, Comp_Flop_Perc, Player_Flop_Perc, Comp_Turn_Perc, Player_Turn_Perc, 
 	Comp_River_Perc, Player_River_Perc, Round_Completed, Stacked, FB_Name, FB_ID, Start_Chips, Email) 
values('" . $Guid . "'," . $Score . ",'" . $C1 . "','" . $C2 . "','" . $B1 . "','" . $B2 . "','" . $B3 . "','"
    . $B4 ."','" . $B5 . "','" . $P1 . "','" . $P2 . "'," . $Button . "," . $Percentile . ","
    . $ShowDown . ",'" . $Date . "','" . $IPaddress . "'," . $BigBet . "," . $CompRaise_PF . ","
    . $PlayerRaise_PF . "," . $Last_Raise_PF . "," . $Bet_PF . "," . $CompRaise_Flop . ","
    . $PlayerRaise_Flop . "," . $Last_Raise_Flop . "," . $Bet_Flop . "," . $CompRaise_Turn . ","
    . $PlayerRaise_Turn . "," . $Last_Raise_Turn . "," . $Bet_Turn . "," . $CompRaise_River . ","
    . $PlayerRaise_River . "," .  $Last_Raise_River . "," . $Bet_River . "," . $GameHandNum . ","
    . $Comp_PF_Value . "," . $Player_PF_Value . "," . $Comp_Flop_Perc . "," . $Player_Flop_Perc . ","
    . $Comp_Turn_Perc . "," . $Player_Turn_Perc . "," . $Comp_River_Perc . "," .  $Player_River_Perc . ","
    . $Round_Completed . "," . $Stacked . ",'" . $FB_Name . "','" . $FB_ID . "'," . $Start_Chips . ",'" . $Email . "')";

$result = mysqli_query($con, $sql);
mysqli_close($con);
