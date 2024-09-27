Proiect GlobalWaves  - Etapa 2 - Voicu Alexa-Andreea 322CD


(For the first stage)

My implementation begins in the Main Class, in which the input files are parsed and the Input.
Objects are created. I start off by reading the input commands and using the GenerateCommands
method to create the specific commands objects based on their type. For each type of input entity
I created my own class, so that it can be easily extendable and relevant fields can be added to
help with the overall implementation. The general flow of the implementation is that once a set of 
commands is read, the query method in the database will call their respective execute methods with 
the necessary parameters, sequentially. Every command will add an ObjectNode to the output with all
the information that the command needs to show.

One thing I noticed among all the commands is that all of them have at least two fields in common: a 
timestamp and a type, therefore I decided to implement an extendable class called Command that 
allows me to create specific classes for all the different command types. This helps because if any
new commands are introduced in the next stage of the project; i can easily create new classes that
extend the general one. 

Another important aspect in the implementation is that every command is associated with a user and
every change or inquiry made should be from a user's perspective. Therefore, every user needs to 
have its own search bar, music player, etc. A solution that i found for this problem is implementing
a new class called DatabaseUser which stores all the relevant information for a certain user.
In order to store all the data in a neat manner, i created my own database, which follows a Singleton
pattern. This class stores all the input data, including the commands. This approach allows me to
keep track of every input in one place, and if needed, to extend it's functionality by adding more
audio file types, or other entities. The database makes the implementation easier to understand, and
creates a starting point for the execution of the commands, because every command's execute method
is being called from the query method in the database. This allows the execute method in the command
classes to only focus on what happens from the user's perspective, rather than having to search
for the data that is needed.

One of the challenges that i faced in the beginning was finding an elegant solution to search among
the different audio types, therefore i created an extandble class called SearchCommands. This allows
me to add more audio types of search commands, if needed in the next stage. Every type of search command
has its own filter class, so that if the filters change, or more filters are added, it can be easily
modified. The actual searching was yet another problem, because it seemed difficult and time consuming
to iterate through the list of audio files and check for each and every one of them if the filters
matched. The solution that I found for this was using streams and their built-in method filter,
which allowed me to create a lambda expression that verifies if the filters are matched and also
limits to the first 5 results. This functionality can be extended if more filters are added or if we
want to save more search results. 

Another issue that i faced was the status command, specifically for the playlists and podcasts.
For playlists it was easier, because I had to find a way to find out which song was playing at any
given moment. My solution was rather mathematical, in the sense that i created a method inside the
DatabasePlaylist class that iterates through every song in the playlist, and given the passed time
from the playlist, will find out which song is playing, by adding every song's duration. The passed
time itself was a bit more tricky to calculate, because I had to take into account if the loaded audio
was paused or playing. If it was paused, the time passed needed to be the time it was paused - the time
it started playing.

By far, the most complicated thing to figure out was managing the podcast playing times. Not only
did i have to find what episode was playing at any given time, but also had to remember for every podcast,
where the user left off in the previous playing. I decided to create a PodcastTuple class that would
help me save that information for every user. From there I created a field in the DatabaseUser which
keeps them organised for every podcast that the user ever played. The idea behind how to actually update
the starting times of every podcast is quite simple: the start time of the loaded podcast will be modified
every time the user loads it. Therefore, by keeping in mind how much of the podcast the user actually
played I can simulate that it actually started playing the audio files, before the actual load command,
so that it can use the same method used for playlists to find out which episode is playing: iterating
through the episode's durations. Although the idea was simple, it was quite difficult to implement,
because of all the different passed times i had to remember and update constantly, and the starting
times that had to be modified. The PodcastTuple object needed to be updated in every load command
and pause command.

With this idea in mind to work with the start time timestamps, i decided to apply the same method for
the Next, Prev, Forward and Backward commands, but a bit more extensive, the way that i also needed
to store some other "times" like the previous playing episode duration. Based on that i was able to
simulate the user skipping and going back on the audio files.

The shuffle command was yet another problem because i couldn't keep the same implementation for the
song finding. Because every command is executed from a user's perspective i decided to create a list
of the playlists that are shuffled by the user, because modifying the playlist itself would have been
a mistake. Therefore, in the shuffle command i created a shuffled playlist using the given seed and
simulated that the currently playing song is actually playing from the shuffled playlist and i removed
all the songs before the one playing so it can be easier to access the status, without having to 
remember where the song actually is in the shuffled playlist. When the status needed to be shown, i
would calculate the song playing, from the shuffled playlists list. When unshuffling i needed to come
back to the original playlist, but keeping in mind what song is currently playing and how much time
has passed from it. To solve this, i created a method that calculates the total playing time up to
a certain song, which i used to simulate the change of playlist from the shuffled one to the original one.

In order to solve the general statistics commands i needed to find a way to store how many users
liked/followed the audio files. The solution that i found was adding a field in every audio file
of this sort that i incremented everytime a user gave the follow/like command. The same goes for the
users, in the DatabaseUser class I stored two lists that contain the liked and followed audio files.
This helped me in the implementation because it was easier to create the ShowPreferredSongs commands,
with the posibility of extending the same command for playlists.




(For the second stage)

For starters, i updated how the commands were created, in the sense that i used a Factory Design 
Pattern to generate the commands based on their type and get a cleaner code. This can be applied 
because the treeToValue() method returns an object of the specific command.

For the cases in which the user was offline, two things needed to be taken into account: the player
status at the time of the switch and the fact that he was no longer able to give commands. So for 
this i stored the timestamp at which the user switched to offline, so that the time left from the
audio remained the same, while also making sure that the player will not clear, even though the
time for the playing of the audio passed. For the second part, i created a method inside the 
Database that verifies if the user given in the command is online and if it isn't, then an output
node is created and added to the outputs. This makes it easier to extend, because it can be 
included for any new command that might need it in the next stage. This method is then further
called and based on that the command is executed or not.

One of the changes i had to implement in the project was the addition of 2 new types of users :
artists and host, that had specific commands and attributes, so my solution was creating 2 classes
that extended the DatabaseUser class. The special users do not include the same functionalities as 
the normal ones, so I could just ignore all of the attributes that they inherited from the extended
class and just continue to add the specific attributes that each type of special user has. But i 
needed to be able to iterate through the list of users and know if a user is a normal one or an 
artist or host. I needed this for the getAllUsers and getOnlineUsers so i added an extra field 
that indicated the relevance in the output list of the getAllUsers command.

Next thing I implemented was the page system. There are four types of pages each with different
content, so I had to keep track of them by creating the Page class that would be extended by all
types of pages and that could be extended further, if new pages are added in the next stage. In 
order to figure out how to store what page a specific user is on I created a field in the Database
User Class, which was the type of page that the user was on and the owner of that page.

For an easier way to create and output structure for the show Albums command and the show Podcast
Commands, i decided that i could create 2 classes that would store all the necessary information 
for both types, in a formatted way, so that the command implementation would be more extandable
and will have less logic in it.

Another challenge i faced when implementing the special users was the constant need to verify what
type of user is given in the command, and including de neccesary verifications, the same code, over
and over. So, for these commands i created an abstract class that is extended by those and that includes
the methods that make the neccesary verifications about the user and output the error message.
By far, the hardest command to implement from this type was the Remove Album one, because i needed
to take into account if any users are playing the album, or a playlist that contains a song from that
album and for the last one i created a method in the SongsCollection class that would help me spread
the code a bit better and make it more extendable.

The next element i added was the new audio entity, album, which I thought was very similar to a
playlist since it has the same functionalities (next, prev, shuffle repeat). The main similarity is
that they both have a list of songs, so i wanted to obtain a cleaner code and keep the same logic for
both audio entities. In order to achieve that, my solution was that i created the SongsCollection
Class that is extended by both the playlist and the album. This makes operations on albums a lot 
easier to implement, since all the operations on the playlist and album are based on the list of
songs. From there, i moved all the methods that the playlist class initially contained and 
refactored all the commands so that the operations would be made on a SongsCollection Object.

The Delete User Command was a difficult task to implement, because of all the cases that had to be
taken into consideration when deciding if a user can be deleted or not. In order to create a cleaner
code i decided to implement specific methods in every user class that would determine if the user
could be deleted. To ease my work, i decided to add an extra field in the player that stores the 
owner of the file that is currently loaded, so that i can easily check if anyone is playing an 
audio file owned by the user that i am trying to delete.

In order to solve the tests, i had to implement the repeat command, which was a challenge, mainly
because of the status command. I had to figure out a way to store which song is currently on repeat
for the songs collections, so my solution was: every time a collection would change it's status to
repeat current song, i would store the timestamp at which the playing of the song started and the
name of the song playing. This way, in the status command it was easier for me to calculate exactly
how many times the songs was repeated and calculate the time left. For the song audio repeat, the
most difficult part was taking into account when the song would change the status back to no repeat,
so for that i doubled the time that the song was allocated in the player and modified the 
clearMusicPlayer method so that it would reset the status back to 0 if there was left less than half
of the allocated time.



