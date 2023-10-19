<?php

namespace App\Controllers;

class LaguPujian extends BaseController
{
    public function index()
    {
		$laguPujianModel = new \App\Models\LaguPujianModel();
		$data['laguPujian'] = $laguPujianModel->findall();
        return view('data', $data);
    }
	
	public function get()
    {
		$laguPujianModel = new \App\Models\LaguPujianModel();
		$id = $this->request->getGet('id');
		$search = $this->request->getGet('search');
		$limit = $this->request->getGet('limit');
		$type = $this->request->getGet('type');
		if($id)
			$data['laguPujian'] = $laguPujianModel->find($id);
		elseif(!$type&&$search)
		{
			$data['laguPujian'] = $laguPujianModel->select('id, title, artist, singer')->like('title', $search)
												->orLike('artist', $search)->orLike('singer', $search)->orLike('category', $search)->findall();
		}
		elseif($type)
		{
			$type = strtoupper($type);
			if($type!='KJ'&&$type!='PKJ'&&$type!='NKB'){
				if($limit){
					$data['laguPujian'] = $laguPujianModel->where('type', 'LPL')->limit($limit)->find();
				}
				elseif($search){
					$data['laguPujian'] = $laguPujianModel->select('id, title, artist, singer')->groupStart()->like('title', $search)
											->orLike('artist', $search)->orLike('singer', $search)->orLike('category', $search)->groupEnd()
											->where('type', 'LPL')->findall();
				}
				else{
					$data['laguPujian'] = $laguPujianModel->where('type', 'LPL')->findall();
				}
			} else {
				if($limit){
					$data['laguPujian'] = $laguPujianModel->like('title', $type, 'after')->limit($limit)->find();
				}
				elseif($search){
					$data['laguPujian'] = $laguPujianModel->select('id, title, artist, singer')->groupStart()->like('title', $search)
											->orLike('artist', $search)->orLike('singer', $search)->orLike('category', $search)->groupEnd()
											->like('title', $type, 'after')->findall();
				}
				else{
					$data['laguPujian'] = $laguPujianModel->like('title', $type, 'after')->findall();
				}
			}
		}
		elseif(!$type&&$limit)
			$data['laguPujian'] = $laguPujianModel->limit($limit)->find();
		else
			$data['laguPujian'] = $laguPujianModel->findall();
        echo json_encode($data['laguPujian']);
    }
	
	public function getLast()
	{
		$laguPujianModel = new \App\Models\LaguPujianModel();
		$data['last'] = $laguPujianModel->select('id')->orderBy('id',"DESC")->limit(1)->find();
		return view('last', $data);
	}
	
	public function getAll()
    {
		$laguPujianModel = new \App\Models\LaguPujianModel();
		$list = $laguPujianModel->findall();
		$data = array();
		$no = 0;
        foreach ($list as $laguPujian) {
            $no++;
            $row = array();
			$row[] = $laguPujian['id'];
            $row[] = $no;
            $row[] = $laguPujian['title'];
            $row[] = $laguPujian['type'];
            $row[] = $laguPujian['artist'];
            $row[] = $laguPujian['singer'];
            $row[] = $laguPujian['category'];
            $row[] = $laguPujian['year'];
            $row[] = $laguPujian['lyric'];
            $row[] = $laguPujian['image_url'];
            $row[] = $laguPujian['file_url'];
 
            $data[] = $row;
        }
		$output = array(
                        "data" => $data,
                );
		echo json_encode($output);
    }
	
	public function add(){
        $image = $this->request->getFile('image');
		$image = $this->upload($image, 'img');
        $file = $this->request->getFile('file');
		$file = $this->upload($file, 'song');
		if(!$image || !$file){
			$result['message'] = 'Insert Failed';
			$result['status'] = false;
			echo json_encode($result);
			die();
		}
		$data =[
			'title' => $this->request->getPost('title'),
			'type' => $this->request->getPost('type'),
			'artist' => $this->request->getPost('artist'),
			'singer' => $this->request->getPost('singer'),
			'category' => $this->request->getPost('category'),
			'year' => $this->request->getPost('year'),
			'lyric' => $this->request->getPost('lyric'),
			'image_url' => $image,
			'file_url' => $file,
		];
		$model = new \App\Models\LaguPujianModel();
		
		if ($model->insert($data)){
			$result['message'] = 'Insert Successfully';
			$result['status'] = true;
		} else {
			$result['message'] = 'Insert Failed';
			$result['status'] = false;
		}
		echo json_encode($result);
	}
	public function edit(){
		$model = new \App\Models\LaguPujianModel();
		
		$id = $this->request->getPost('id');
		$image = $this->request->getFile('image');
		$old_image = $model->select('image_url')->find($id);
		if(!$image->getClientName())
			$image = $old_image['image_url'];
		else
			$image = $this->upload($image, 'img', $old_image['image_url']);
		
        $file = $this->request->getFile('file');
		$old_file = $model->select('file_url')->find($id);
		
		if(!$file->getClientName())
			$file = $old_file['file_url'];
		else
			$file = $this->upload($file, 'song', $old_file['file_url']);
		
		if(!$image || !$file){
			$result['message'] = 'Insert Failed';
			$result['status'] = false;
			echo json_encode($result);
			die();
		}
		$data =[
			'title' => $this->request->getPost('title'),
			'type' => $this->request->getPost('type'),
			'artist' => $this->request->getPost('artist'),
			'singer' => $this->request->getPost('singer'),
			'category' => $this->request->getPost('category'),
			'year' => $this->request->getPost('year'),
			'lyric' => $this->request->getPost('lyric'),
			'image_url' => $image,
			'file_url' => $file,
		];
		if ($model->update($id, $data)){
			$result['message'] = 'Edit Successfully';
			$result['status'] = true;
		} else {
			$result['message'] = 'Edit Failed';
			$result['status'] = false;
		}	
		echo json_encode($result);
	}
	public function delete(){
		$id = $this->request->getPost('id');
		$model = new \App\Models\LaguPujianModel();
		if ($model->delete($id)){
			$result['message'] = 'Delete Successfully';
			$result['status'] = true;
		} else {
			$result['message'] = 'Delete Failed';
			$result['status'] = false;
		}
		echo json_encode($result);
	}
	public function upload($file, $path, $old_file=null){
		$data = $file->getClientName();
		if($old_file){
			unlink(ROOTPATH . 'public\\' . $path . '\\' . $old_file);
		}
        if (!$file->move(ROOTPATH . 'public\\' . $path, null, true)) {
            return false;
        }
        return $data;
	}
}
