# chat-websocket
http://stackoverflow.com/questions/8252323/mysql-closure-table-hierarchical-database-how-to-pull-information-out-in-the-c
http://stackoverflow.com/questions/192220/what-is-the-most-efficient-elegant-way-to-parse-a-flat-table-into-a-tree/

# TODO

- Start adding [bootstrap-markdown.](http://www.codingdrama.com/bootstrap-markdown/)
- Comment editing(on your own comments).
- A working user / user login system
- ~~Comment collapsing~~
- Comment subset loading
  - What happens when you get too many comments?
  - Do you not load new ones, if they aren't under your branch?
  - Get comment_threaded_view working for 
- Refresh only specific changed content
  - For now, use newCommentId.
  - Automatically Scroll to new comment - location.href = "#myDiv";
  - Change them to a new highlighted color, then remove that.
  - Should you scroll when you are currently replying, or wait till after, or not at all?
- Reconnect websocket if disconnected
  - http://stackoverflow.com/questions/3479734/javascript-jquery-test-if-window-has-focus
  