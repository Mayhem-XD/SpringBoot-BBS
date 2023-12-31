package com.ys.sbbs.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.ys.sbbs.entity.Board;
import com.ys.sbbs.entity.Reply;
import com.ys.sbbs.service.BoardService;
import com.ys.sbbs.service.ReplyService;
import com.ys.sbbs.utility.JsonUtil;

@Controller
@RequestMapping("/board")
public class BoardController {
	@Autowired private BoardService boardService;
	@Autowired private ReplyService replyService;
	
	@Value("${spring.servlet.multipart.location}") private String uploadDir;
	
	@GetMapping("/list")
	public String list(@RequestParam(name="p", defaultValue="1") int page,
					   @RequestParam(name="f", defaultValue="title") String field,
					   @RequestParam(name="q", defaultValue="") String query,
					   HttpSession session, Model model) {
		List<Board> list = boardService.listBoard(field, query, page);
		
		int totalBoardCount = boardService.getBoardCount(field, query);
		int totalPages = (int) Math.ceil(totalBoardCount / (double) BoardService.LIST_PER_PAGE);
		int startPage = (int) Math.ceil((page-0.5)/BoardService.PAGE_PER_SCREEN - 1) * BoardService.PAGE_PER_SCREEN + 1;
		int endPage = Math.min(totalPages, startPage + BoardService.PAGE_PER_SCREEN - 1);
		List<String> pageList = new ArrayList<String>();
		for (int i = startPage; i <= endPage; i++)
			pageList.add(String.valueOf(i));
		
		session.setAttribute("currentBoardPage", page);
		model.addAttribute("boardList", list);
		model.addAttribute("field", field);
		model.addAttribute("query", query);
		model.addAttribute("today", LocalDate.now().toString());
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("pageList", pageList);
		return "board/list";
	}

	@GetMapping("/detail/{bid}/{uid}")
	public String detail(@PathVariable int bid, @PathVariable String uid, String option,
						 HttpSession session, Model model) {
		// 본인이 조회한 경우 또는 댓글 작성후에는 조회수를 증가시키지 않음
		String sessionUid = (String) session.getAttribute("sessUid");
		if (!uid.equals(sessionUid) && (option==null || option.equals("")))
			boardService.increaseViewCount(bid);
		
		Board board = boardService.getBoard(bid);
		String jsonFiles = board.getFiles();
		if (!(jsonFiles == null || jsonFiles.equals(""))) {
			JsonUtil ju = new JsonUtil();
			List<String> fileList = ju.jsonToList(jsonFiles);
			model.addAttribute("fileList", fileList);
		}
		model.addAttribute("board", board);
		List<Reply> replyList = replyService.getReplyList(bid);
		model.addAttribute("replyList", replyList);
//		return "board/detail";
		return "board/detailEditor";
	}
	
	@GetMapping("/write")
	public String writeForm() {
//		return "board/write";
		return "board/writeEditor";
	}
	
	@PostMapping("/write")
	public String writeProc(MultipartHttpServletRequest req, HttpSession session) {
		String title = req.getParameter("title");
		String content = req.getParameter("content");
		List<MultipartFile> uploadFileList = req.getFiles("files");
		String sessionUid = (String) session.getAttribute("sessUid");

		List<String> fileList = new ArrayList<>();
		for (MultipartFile part: uploadFileList) {
			if (part.getContentType().contains("octet-stream"))		// 첨부 파일이 없는 경우 application/octet-stream
				continue;
			String filename = part.getOriginalFilename();
			String uploadPath = uploadDir + "upload/" + filename;
			try {
				part.transferTo(new File(uploadPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
			fileList.add(filename);
		}
		JsonUtil ju = new JsonUtil();
		String files = ju.listToJson(fileList);
		
		Board board = new Board(sessionUid, title, content, files);
		System.out.println(board);
		boardService.insertBoard(board);
		return "redirect:/board/list?p=1&f=&q=";
	}
	
	@GetMapping("/delete/{bid}")
	public String delete(@PathVariable int bid, Model model) {
		model.addAttribute("bid", bid);
		return "board/delete";
	}
	
	@GetMapping("/deleteConfirm/{bid}")
	public String deleteConfirm(@PathVariable int bid, HttpSession session) {
		boardService.deleteBoard(bid);
		return "redirect:/board/list?p=" + session.getAttribute("currentBoardPage") + "&f=&q=";
	}
	
	@GetMapping("/update/{bid}")
	public String update(@PathVariable int bid, Model model, HttpSession session) {
		Board board = boardService.getBoard(bid);
		board.setTitle(board.getTitle().replace("\"", "&quot;"));
		model.addAttribute(board);
		
		String uploadedFiles = board.getFiles().trim();
		if (uploadedFiles != null && uploadedFiles.contains("list")) {
			JsonUtil ju = new JsonUtil();
			List<String> fileList = ju.jsonToList(uploadedFiles);
			session.setAttribute("fileList", fileList);
		}
		
		
		return "board/updateEditor";
	}
	@PostMapping("/update")
	public String updateProc(MultipartHttpServletRequest req, HttpSession session, Model model) {
		String sessionUid = (String) session.getAttribute("sessUid");
		int bid = Integer.parseInt(req.getParameter("bid"));
		String title = req.getParameter("title");
		String content = req.getParameter("content");
		
		List<String> fileList = (List<String>) session.getAttribute("fileList");
		
		if (fileList != null && fileList.size() > 0) {
			String[] delFiles = req.getParameterValues("delFile");
			if (delFiles != null && delFiles.length > 0) {
				for (String delFile: delFiles) {
					fileList.remove(delFile);			// fileList에서 삭제
					File df = new File(uploadDir + delFile);	// 실제 파일 삭제
					df.delete();
				}
			}
		}
		
		List<MultipartFile> uploadFileList = req.getFiles("files");
		for (MultipartFile part: uploadFileList) {
			if (part.getContentType().contains("octet-stream"))		// 첨부 파일이 없는 경우 application/octet-stream
				continue;
			String filename = part.getOriginalFilename();
			String uploadPath = uploadDir + "upload/" + filename;
			try {
				part.transferTo(new File(uploadPath));
			} catch (Exception e) {
				e.printStackTrace();
			}
			fileList.add(filename);
		}
		String files = new JsonUtil().listToJson(fileList);
		
		Board board = new Board(bid, title, content, files);
		boardService.updateBoard(board);
		
		return "redirect:/board/detail/"+bid+ "/" + sessionUid;
	}
	
	
}