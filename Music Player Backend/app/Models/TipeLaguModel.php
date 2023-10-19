<?php

namespace App\Models;

use CodeIgniter\Model;

class LaguPujianModel extends Model
{
    protected $table      = 'tipe_lagu';
    protected $primaryKey = 'id';

    protected $useAutoIncrement = true;

    protected $returnType     = 'array';
	
	protected $allowedFields = ['id', 'name'];
	
	public function count_all()
    {
        $this->db;
        return $this->db->table($this->table)->countAllResults();
    }

}