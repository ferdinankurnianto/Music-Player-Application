<?php

namespace App\Controllers;

class TipeLagu extends BaseController
{
    public function index()
    {
		$tipeLaguModel = new \App\Models\TipeLaguModel();
		$data['tipeLagu'] = $tipeLaguModel->orderBy('sorting', 'ASC')->findall();
		echo json_encode($data['tipeLagu']);
    }
}
