<?php

  $hostname = "localhost";
  $username = "ted";
  $dbname = "";

$con = mysqli_connect($hostname, $username, '', $dbname);

if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
}

 $ipAddress = $_SERVER['REMOTE_ADDR'];

$sql="SELECT Score FROM `hh2` ignore index (total_score) WHERE ipaddress = '" . $ipAddress . "'";


$result = mysqli_query($con, $sql);
//$row = mysqli_fetch_array($result);
//echo $row['Score'];


$score = array(0);
while ($row = mysqli_fetch_array($result)) {
    echo($row['Score'] * -1) . ',';
}
echo 0;
