<?php

    $hostname = "localhost";
    $username = "ted";
    $dbname = "";
    $con = mysqli_connect($hostname, $username, '', $dbname);
    
    $leaderboardType = $_REQUEST['leaderboardType'];
    $Date = date("Y-m-d H:i:s");

    if (!$con) {
        die('Could not connect: ' . mysqli_error($con));
    }
     
    // array for JSON response
    $response = array();
    
    if ($leaderboardType == 'chips') {
        $sql = "SELECT FB_ID as fb_id, MAX( FB_Name ) as fb_name , SUM( score ) as score
							FROM hh_android
							WHERE fb_id !=  ''
							GROUP BY fb_id
							ORDER BY score
							LIMIT 20";
    }
    
    if ($leaderboardType == 'bb100') {
        $sql = "SELECT FB_ID as fb_id, MAX( FB_Name ) as fb_name , SUM( score ) / count(1) * 5 as score
							FROM hh_android
							WHERE fb_id !=  ''
							GROUP BY fb_id
							ORDER BY score
							LIMIT 20";
    }
    
    if ($leaderboardType == '24') {
        $sql = "SELECT FB_ID as fb_id, MAX( FB_Name ) as fb_name , SUM( score ) as score
							FROM hh_android
							WHERE fb_id !=  '' 
							and time_to_sec(timediff('" . $Date . "', date)) / 3600 <= 24 
							GROUP BY fb_id
							ORDER BY score
							LIMIT 20";
    }
    
    
    // get all scores
    $result = mysqli_query($con, $sql);
    $response["all_scores"] = array();
    
    while ($row = mysqli_fetch_array($result)) {
        $fb_scores = array();
        $fb_scores["fb_name"] = $row["fb_name"];
        $fb_scores["score"] = $row["score"];
        $fb_scores["fb_id"] = $row["fb_id"];
         
        array_push($response["all_scores"], $fb_scores);
    }
    $response["success"] = 1;
    
    // echoing JSON response
    echo json_encode($response);
