<?php

  $hostname = "localhost";
  $username = "ted";
  $dbname = "";
  $con = mysqli_connect($hostname, $username, '', $dbname);

  // use for remote from my pc
  // $hostname = "box969.bluehost.com";



// $user = 'root';
// $password = 'root';
// $db = 'hand_history';
// $host = 'localhost';
// //$port = 8889;

// $con = mysqli_connect(
//    $host,
//    $user,
//    $password,
//    $db
// );



if (!$con) {
    die('Could not connect: ' . mysqli_error($con));
}

  $ipAddress = $_SERVER['REMOTE_ADDR'];

$sql="SELECT * FROM `Total User Summary` WHERE ipaddress = '" . $ipAddress . "'";



$sql = "select `IPaddress` AS `IPaddress`,count(1) AS `HandsPlayed`,
(sum(`Score`) * -(1)) AS `Score`,(((sum(`Score`) / 
  count(1)) * 5) * -(1)) AS `BBper100`,(count((case when (((`B1` <> '') 
    and (`Button` = 0)) or ((`Score` < 0) and (`Button` = 0))) then 1 end))
     / count((case when (`Button` = 0) then 1 end))) AS `VPIP`
  ,(count((case when (`ShowDown` = 1) then 1 end)) / count((case when (`B1` <> '') 
    then 1 end))) AS `SD_perc`,(count((case when ((`ShowDown` = 1) 
      and (`Score` < 0)) then 1 end)) / count((case when (`ShowDown` = 1) 
    then 1 end))) AS `SD_Win_perc` from hh2 WHERE ipaddress = '" . $ipAddress . "' 
     group by `IPaddress` order by count(1) desc";

//player stats
echo '
<div>
<table class = "table table-striped table-bordered table-hover table-condensed" border="5">
<tr>
<th>Your First 4 of IPaddress</th>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>Win Rate (BB/100)</th>
<th>Voluntarily put money into pot from small blind %</th>
<th>Went to showdown when seeing the flop %</th>
<th>Won at showdown %</th>

</tr>';

$result = mysqli_query($con, $sql);
// $result2 = $result;
//$row = mysqli_fetch_array($result); //$result
//while($row = mysqli_fetch_array($result))
$row = mysqli_fetch_array($result);
  
  echo "<tr>";
  echo "<td>" . substr($row['IPaddress'], 0, 5) . "</td>";
  echo "<td>" . $row['HandsPlayed'] . "</td>";
  echo "<td>" . $row['Score']  . "</td>";
  echo "<td>" . round($row['BBper100'], 1) . "</td>";
  echo "<td>" . round($row['VPIP'] * 100, 1)  . "</td>";
  echo "<td>" . round($row['SD_perc'] * 100, 1)  . "</td>";
  echo "<td>" . round($row['SD_Win_perc'] * 100, 1)  . "</td>";
  echo "</tr>";
echo "</table>";
echo "</div>";


echo '<div id="chart_div" style="width: 900px; height: 500px;"></div>';


//last 24 hours fb
 $sql = "SELECT FB_Name, HandsPlayed, Score, BBper100 
		FROM `last 24 hours fb` order by score desc limit 5";

$sql = "SELECT `FB_Name` AS `FB_Name`,`FB_ID` AS `fb_id`,count(1) AS `HandsPlayed`
,(sum(`Score`) * -(1)) AS `Score`,((sum(`Score`) / count(1)) * -(5)) AS `BBper100` 
from `hh2` where ((timestampdiff(HOUR,`Date`,now()) <= 24) and
 (`FB_Name` <> '')) group by `FB_Name`,`FB_ID` 
order by (sum(`Score`) * -(1)) desc limit 5";


$result = mysqli_query($con, $sql);

echo "<div>";
echo '<p id = "thead">Last 24 hours Top 5 Facebook Users Leaderboard by total winnings</p>';

echo '<table class = "table table-striped table-bordered table-hover table-condensed"  border="1">
<tr>
<th>Facebook Name</th>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>BB per 100 </th>
</tr>';
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row["FB_Name"] . "</td>";
    echo "<td>" . $row["HandsPlayed"] . "</td>";
    echo "<td>" . $row["Score"] . "</td>";
    echo "<td>" . round($row["BBper100"], 1) . "</td>";
    echo "</tr>";
}
echo "</table>";
 echo "</div>";




//last 24 hours
 $sql = "SELECT left(ipaddress, 5) as firstIP, HandsPlayed, Score, BBper100 
		FROM `last 24 hours` order by score desc limit 5";

$sql = "SELECT left(ipaddress, 5) as firstIP,count(1) AS `HandsPlayed`,
    (sum(`Score`) * -(1)) AS `Score`,((sum(`Score`) / count(1)) * -(5)) AS `BBper100` 
    from `hh2` where (abs(timestampdiff(HOUR,now(),`Date`)) <= 24) 
    group by `IPaddress` order by Score desc limit 5";

$result = mysqli_query($con, $sql);

echo "<div>";
echo '<p id = "thead">Last 24 hours Top 5 Leaderboard by total winnings</p>';

echo "<table class = 'table table-striped table-bordered table-hover table-condensed' border='1'>
<tr>
<th>First 4 of IPaddress</th>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>BB per 100 </th>
</tr>";
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row['firstIP'] . "</td>";
    echo '<td>' . $row['HandsPlayed'] . '</td>';
    echo '<td>' . $row['Score'] . "</td>";
    echo "<td>" . round($row['BBper100'], 1) . "</td>";
    echo "</tr>";
}
echo "</table>";
 echo "</div>";




//winnings by bb/100 min 200 hands
$sql = "SELECT left(ipaddress, 5) as firstIP, HandsPlayed, Score, BBper100 
		FROM `Total User Summary` where HandsPlayed > 200 order by BBper100 desc limit 10";


$sql = " SELECT left(ipaddress, 5)  as firstIP, count(1) AS `HandsPlayed`,
(sum(`Score`) * -(1)) AS `Score`,(((sum(`Score`) / 
  count(1)) * 5) * -(1)) AS `BBper100`
   from hh2 
     group by left(ipaddress, 5) 
     HAVING COUNT(1) > 200 
     order by BBper100 desc limit 10";

$result = mysqli_query($con, $sql);
echo "<div>";
echo '<p id = "thead">Top 10 Leaderboard by BB per 100 (Minimum 200 hands played)</p>';

echo "<table class = 'table table-striped table-bordered table-hover table-condensed' border='1'>
<tr>
<th>First 4 of IPaddress</th>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>BB per 100 </th>
</tr>";
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row['firstIP'] . "</td>";
    echo '<td>' . $row['HandsPlayed'] . '</td>';
    echo '<td>' . $row['Score'] . "</td>";
    echo "<td>" . round($row['BBper100'], 1) . "</td>";
    echo "</tr>";
}
echo "</table>";
 echo "</div>";

//total winnings
$sql = "SELECT left(ipaddress, 5) as firstIP, HandsPlayed, Score, BBper100 
		FROM `Total User Summary` order by score desc limit 10";

$sql = "SELECT left(ipaddress, 5) as firstIP,count(1) AS `HandsPlayed`,
(sum(`Score`) * -(1)) AS `Score`,(((sum(`Score`) / 
  count(1)) * 5) * -(1)) AS `BBper100`
   from hh2  
     group by left(ipaddress, 5) order by score desc limit 10";

$result = mysqli_query($con, $sql);

echo "<div>";
echo '<p id = "thead">Top 10 Leaderboard by total winnings</p>';

echo "<table class = 'table table-striped table-bordered table-hover table-condensed' border='1'>
<tr>
<th>First 4 of IPaddress</th>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>BB per 100 </th>
</tr>";
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row['firstIP'] . "</td>";
    echo '<td>' . $row['HandsPlayed'] . '</td>';
    echo '<td>' . $row['Score'] . "</td>";
    echo "<td>" . round($row['BBper100'], 1) . "</td>";
    echo "</tr>";
}
echo "</table>";
 echo "</div>";

//put computer winnings
 $date = date('Y-m-d H:i:s');
 $sql = "SELECT count(1) as HandsPlayed, sum(score) as Score, sum(score) / count(1) * 5 as BBper100 
		FROM `hh2` where timestampdiff(hour, date, '" .$date . "') <= 24";
$result = mysqli_query($con, $sql);

 
echo "<div>";
echo '<p id = "thead">Computer winnings last 24 hours</p>';
echo "<table class = 'table table-hover' border='1'>
<tr>
<th>Hands Played</th>
<th>Total Winnings</th>
<th>BB per 100 </th>
</tr>";
while ($row = mysqli_fetch_array($result)) {
    echo '<td>' . $row['HandsPlayed'] . '</td>';
    echo '<td>' . $row['Score'] . "</td>";
    echo "<td>" . round($row['BBper100'], 1) . "</td>";
    echo "</tr>";
}
echo "</table>";
echo "</div>";


//player best/worst hands
echo "<div>";
echo '<p id = "thead">Top 5 hands where you won the most</p>';

echo "<table class = 'table table-striped table-bordered table-hover table-condensed' border='1'>
<tr>
<th>Amount Won</th>
<th>Player Card 1</th>
<th>Player Card 2</th>
<th>Board Card 1</th>
<th>Board Card 2</th>
<th>Board Card 3</th>
<th>Board Card 4</th>
<th>Board Card 5</th>
<th>Computer Card 1</th>
<th>Computer Card 2</th>

</tr>";

$sql = "SELECT Score, C1, C2, b1, b2, b3, b4, b5, p1, p2 
		FROM `hh2` WHERE ipaddress = '" . $ipAddress . "' order by score limit 5";

$result = mysqli_query($con, $sql);
echo "<tr>";
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row['Score'] * -1 . "</td>";
    echo '<td id = "pc">' . $row['p1'] . '</td>';
    echo '<td id = "pc">' . $row['p2'] . "</td>";
    echo "<td>" . $row['b1'] . "</td>";
    echo "<td>" . $row['b2'] . "</td>";
    echo "<td>" . $row['b3'] . "</td>";
    echo "<td>" . $row['b4'] . "</td>";
    echo "<td>" . $row['b5'] . "</td>";
    echo '<td id = "cc">' . $row['C1'] . "</td>";
    echo '<td id = "cc">' . $row['C2'] . "</td>";
    echo "</tr>";
}

echo "</table>";

echo "</div>" ;


echo "<div>";
echo '<p id = "thead">Top 5 hands where you lost the most</p>';

echo "<table class = 'table table-striped table-bordered table-hover table-condensed' border='1'>
<tr>
<th>Amount Lost</th>
<th>Player Card 1</th>
<th>Player Card 2</th>
<th>Board Card 1</th>
<th>Board Card 2</th>
<th>Board Card 3</th>
<th>Board Card 4</th>
<th>Board Card 5</th>
<th>Computer Card 1</th>
<th>Computer Card 2</th>

</tr>";

$sql = "SELECT Score, C1, C2, b1, b2, b3, b4, b5, p1, p2 
		FROM `hh2` WHERE ipaddress = '" . $ipAddress . "' order by score desc limit 5";

$result = mysqli_query($con, $sql);
echo "<tr>";
while ($row = mysqli_fetch_array($result)) {
    echo "<td>" . $row['Score'] . "</td>";
    echo '<td id = "pc">' . $row['p1'] . '</td>';
    echo '<td id = "pc">' . $row['p2'] . "</td>";
    echo "<td>" . $row['b1'] . "</td>";
    echo "<td>" . $row['b2'] . "</td>";
    echo "<td>" . $row['b3'] . "</td>";
    echo "<td>" . $row['b4'] . "</td>";
    echo "<td>" . $row['b5'] . "</td>";
    echo '<td id = "cc">' . $row['C1'] . "</td>";
    echo '<td id = "cc">' . $row['C2'] . "</td>";
    echo "</tr>";
}

echo "</table>";

echo "</div>" ;


mysqli_close($con);
?>

