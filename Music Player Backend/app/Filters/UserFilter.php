<?php namespace App\Filters;

use CodeIgniter\HTTP\RequestInterface;
use CodeIgniter\HTTP\ResponseInterface;
use CodeIgniter\Filters\FilterInterface;
use App\Models\APIModel;

class UserFilter implements FilterInterface
{
    public function before(RequestInterface $request, $arguments = null)
    {
        // Do something here
        if(!empty($request->getServer()["HTTP_APIKEY"])){
            $model = new APIModel;
            $api_key = $model->where('active', 1)->first();
            if(empty($api_key)){
                return redirect()->to(base_url(''));
            }
            else{
                if($request->getServer()["HTTP_APIKEY"] != $api_key['api_key']){
                    return redirect()->to(base_url(''));
                }
            }
        }
        else{
            if(!session()->get('logged_in')){
                return redirect()->to(base_url('login'));
            }
        }
    }

    //--------------------------------------------------------------------

    public function after(RequestInterface $request, ResponseInterface $response, $arguments = null)
    {
        // Do something here
    }
}