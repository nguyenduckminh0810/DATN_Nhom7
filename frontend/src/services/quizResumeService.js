import api from '@/utils/axios'

/**
 * ✅ Service xử lý Resume Quiz
 */
export const quizResumeService = {
  
  /**
   * ✅ Kiểm tra xem user có attempt dở nào cho quiz không
   */
  async checkInProgressAttempt(quizId) {
    try {
      const response = await api.get(`/quiz-resume/check/${quizId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi kiểm tra attempt dở:', error)
      throw error
    }
  },
  
  /**
   * ✅ Lấy thông tin chi tiết để resume quiz
   */
  async resumeAttempt(attemptId) {
    try {
      const response = await api.get(`/quiz-resume/resume/${attemptId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi resume attempt:', error)
      throw error
    }
      },
  
  /**
   * ✅ Lưu tiến độ làm quiz (auto-save)
   */
  async saveProgress(attemptId, questionIndex, timeRemaining, answers) {
    try {
      const payload = {
        questionIndex,
        timeRemaining,
        answers
      }
      
      const response = await api.post(`/quiz-resume/save-progress/${attemptId}`, payload)
      return response.data
    } catch (error) {
      console.error('Lỗi khi lưu tiến độ:', error)
      throw error
    }
  },
  
  /**
   * ✅ Tạo attempt mới (khi user chọn làm lại)
   */
  async createNewAttempt(quizId) {
    try {
      const response = await api.post(`/quiz-resume/new-attempt/${quizId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi tạo attempt mới:', error)
      throw error
    }
  },
  
  /**
   * ✅ Lấy đáp án đã chọn
   */
  async getAnswers(attemptId) {
    try {
      const response = await api.get(`/quiz-resume/answers/${attemptId}`)
      return response.data
    } catch (error) {
      console.error('Lỗi khi lấy đáp án:', error)
      throw error
    }
  }
}

/**
 * ✅ Local Storage Service cho Progress
 */
export const progressStorageService = {
  
  /**
   * ✅ Lưu tiến độ vào localStorage
   */
  saveProgress(quizId, attemptId, questionIndex, timeRemaining, answers) {
    try {
      const key = `quiz_progress_${quizId}_${attemptId}`
      const data = {
        quizId,
        attemptId,
        questionIndex,
        timeRemaining,
        answers,
        timestamp: Date.now()
      }
      
      localStorage.setItem(key, JSON.stringify(data))
      return true
    } catch (error) {
      console.error('Lỗi khi lưu vào localStorage:', error)
      return false
    }
  },
  
  /**
   * ✅ Lấy tiến độ từ localStorage
   */
  getProgress(quizId, attemptId) {
    try {
      const key = `quiz_progress_${quizId}_${attemptId}`
      const data = localStorage.getItem(key)
      
      if (!data) return null
      
      const progress = JSON.parse(data)
      
      // Kiểm tra xem dữ liệu có quá cũ không (24 giờ)
      const oneDay = 24 * 60 * 60 * 1000
      if (Date.now() - progress.timestamp > oneDay) {
        localStorage.removeItem(key)
        return null
      }
      
      return progress
    } catch (error) {
      console.error('Lỗi khi lấy từ localStorage:', error)
      return null
    }
  },
  
  /**
   * ✅ Xóa tiến độ khỏi localStorage
   */
  removeProgress(quizId, attemptId) {
    try {
      const key = `quiz_progress_${quizId}_${attemptId}`
      localStorage.removeItem(key)
      return true
    } catch (error) {
      console.error('Lỗi khi xóa khỏi localStorage:', error)
      return false
    }
  },
  
  /**
   * ✅ Xóa tất cả tiến độ của một quiz
   */
  removeAllProgress(quizId) {
    try {
      const keys = Object.keys(localStorage)
      const quizKeys = keys.filter(key => key.startsWith(`quiz_progress_${quizId}_`))
      
      quizKeys.forEach(key => localStorage.removeItem(key))
      return true
    } catch (error) {
      console.error('Lỗi khi xóa tất cả tiến độ:', error)
      return false
    }
  }
}

/**
 * ✅ Auto-save Service
 */
export const autoSaveService = {
  
  /**
   * ✅ Bắt đầu auto-save
   */
  startAutoSave(quizId, attemptId, saveCallback, intervalMs = 30000) {
    const intervalId = setInterval(async () => {
      try {
        await saveCallback()
      } catch (error) {
        console.error('Lỗi auto-save:', error)
      }
    }, intervalMs)
    
    return intervalId
  },
  
  /**
   * ✅ Dừng auto-save
   */
  stopAutoSave(intervalId) {
    if (intervalId) {
      clearInterval(intervalId)
    }
  }
}
