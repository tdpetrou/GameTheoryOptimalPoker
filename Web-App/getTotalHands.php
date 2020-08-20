<?php

  $hostname = "localhost";
  $username = "ted";
  $dbname = "";

  $con = mysqli_connect($hostname, $username, '', $dbname, 3306);

if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
}

 $ipAddress = $_SERVER['REMOTE_ADDR'];

$sql="SELECT max(HandNum) AS HandNum FROM `hh2`";

$result = mysqli_query($con, $sql);
$row = mysqli_fetch_array($result);
echo $row['HandNum'] + 1;
