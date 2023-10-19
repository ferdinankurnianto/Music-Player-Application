<?php
namespace App\Controllers;
use App\Models\UserModel;

class User extends BaseController{
	public function __construct(){
	}
	public function index(){
	}
	public function login(){
		echo view('login_view');
	}
	
	public function loginCheck(){
		$name = $this->request->getPost('name');
		$password = $this->request->getPost('password');
		$userModel = new \App\Models\UserModel();
		
		if($userModel->where('password', $password)->where('name', $name)->findAll()){
			$udata = [
				'name' => $name,
				'logged_in' => true
			];
			session()->set($udata);
			$result['status'] = true;
		} else {
			$result['message'] = 'Login Failed';
			$result['status'] = false;
		}
		echo json_encode($result);
	}
	
	public function logout(){
		session()->destroy();
		return redirect()->to('');
	}
}
?>
