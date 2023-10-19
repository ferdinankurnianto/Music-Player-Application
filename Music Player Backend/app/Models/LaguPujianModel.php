<?php

namespace App\Models;

use CodeIgniter\Model;

class LaguPujianModel extends Model
{
    protected $table      = 'lagu_pujian';
    protected $primaryKey = 'id';

    protected $useAutoIncrement = true;

    protected $returnType     = 'array';
	
	protected $allowedFields = ['title', 'type', 'artist', 'singer', 'category', 'year', 'lyric', 'image_url', 'file_url'];
	
	public function count_all()
    {
        $this->db;
        return $this->db->table($this->table)->countAllResults();
    }

}