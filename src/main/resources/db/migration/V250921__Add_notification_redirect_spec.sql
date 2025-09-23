ALTER TABLE `notifications`
  ADD COLUMN `redirect_spec` TEXT NULL
  COMMENT 'NotificationRedirectSpec을 json 형식의 TEXT 로 저장';