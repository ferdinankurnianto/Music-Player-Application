<?php
namespace App\Controllers;
use App\Models\UsersModel;

class Admin extends BaseController{
	public function index(){
		echo view('admin');
	}
}
?>