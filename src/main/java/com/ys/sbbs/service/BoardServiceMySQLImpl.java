package com.ys.sbbs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ys.sbbs.dao.BoardDaoMySQL;
import com.ys.sbbs.entity.Board;

@Service
public class BoardServiceMySQLImpl implements BoardService {

	@Autowired private BoardDaoMySQL boardDao;
	
	@Override
	public Board getBoard(int bid) {
		Board board = boardDao.getBoard(bid);
		return board;
	}

	@Override
	public int getBoardCount(String field, String query) {
		query = "%" + query + "%";
		int count = boardDao.getBoardCount(field, query);
		return count;
	}

	@Override
	public List<Board> listBoard(String field, String query, int page) {
		int offset = (page - 1) * LIST_PER_PAGE;
		query = "%" + query + "%";
		List<Board> list = boardDao.listBoard(field, query, offset);
		return list;
	}

	@Override
	public void insertBoard(Board board) {
		boardDao.insertBoard(board);
	}

	@Override
	public void updateBoard(Board board) {
		boardDao.updateBoard(board);
	}

	@Override
	public void deleteBoard(int bid) {
		boardDao.deleteBoard(bid);
	}

	@Override
	public void increaseViewCount(int bid) {
		String field = "viewCount";
		boardDao.increaseCount(field, bid);
	}

	@Override
	public void increaseReplyCount(int bid) {
		String field = "replyCount";
		boardDao.increaseCount(field, bid);
	}

	@Override
	public void decreaseReplyCount(int bid) {
		boardDao.decreaseReplyCount(bid);
	}

}
