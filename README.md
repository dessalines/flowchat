# chat-websocket

# TODO
- Comment sort broadcasting for peoples votes? Seems excessive, but could be very useful for sorting. 
	- Also avoids having to reload the page.
	- Would require resorting(and repulling) all the parents of that comment, just like editing.
	- Top level ones would require a full resort... seems excessive.
- Reconnect websocket if [disconnected](http://stackoverflow.com/questions/3479734/javascript-jquery-test-if-window-has-focus)
- Front page is for creating discussions
- Toastr / whatever messages for errors, successes.
- Serving multiple discussions
	- Discussions on left
	- Set up like Stash
- Search bar, search for discussions
- Publish markdown-edit as library, [example](http://blog.angular-university.io/how-to-create-an-angular-2-library-and-how-to-consume-it-jspm-vs-webpack/).
- Links, or left bar with discussions? Are you making a reddit alternative, or a slack alternative? Team-based discussions, or thread based?

# Finished
- Top level replying
	- Make it feel like chat, with a bottom expandable bar
- Automatic setting of cookies for anonymous users
- Refresh only specific changed content
  - For now, use newCommentId.
  - Automatically Scroll to new comment - location.href = "#myDiv";
  - Change them to a new highlighted color, then remove that.
  - Should you scroll when you are currently replying, or wait till after, or not at all?
  - Recursion: [1](http://stackoverflow.com/a/2549333/1655478) [2](http://stackoverflow.com/questions/16228467/how-do-i-break-out-of-loops-in-recursive-functions) [3](http://stackoverflow.com/questions/34522306/angular-2-focus-on-newly-added-input-element)
- Comment collapsing
- Start adding [bootstrap-markdown.](http://www.codingdrama.com/bootstrap-markdown/)
- A working user / user login system
  - Create an open system, new users are just labeled as anonymous_aId
  - Comment editing(on your own comments).
  - When user logs in, refresh the session. [BehaviorSubject](http://stackoverflow.com/questions/34376854/delegation-eventemitter-or-observable-in-angular2/35568924#35568924)
- Comment subset loading
  - What happens when you get too many comments?
    - Use comment_breadcrumbs_view where parent_id = [parent_id]
  - Do you not load new ones, if they aren't under your branch?
  - Get comment_threaded_view working for 
  - Implement a max depth based on how it looks, and a goto discussion button
  - [Route params](http://plnkr.co/edit/IcnEzZ0WtiaY5Bpqrq2Y?p=preview) [2](https://github.com/angular/angular/issues/6204)
  - Hierarchical data in SQL [1](http://stackoverflow.com/questions/8252323/mysql-closure-table-hierarchical-database-how-to-pull-information-out-in-the-c) [2](http://stackoverflow.com/questions/192220/what-is-the-most-efficient-elegant-way-to-parse-a-flat-table-into-a-tree/)
- Possibly add range voting?
	- Use default html sliders [1](http://stackoverflow.com/questions/15935837/how-to-display-a-range-input-slider-vertically) [styling](http://danielstern.ca/range.css/#/) [fiddle](http://jsfiddle.net/Mmgxg/)
  