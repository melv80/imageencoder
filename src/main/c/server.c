#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <ctype.h>

int BUFFER_SIZE = 256;
int TOKEN_BUFFER_SIZE= 16;

void error(const char *msg)
{
    perror(msg);
    exit(1);
}

/**
called in a different thread, handles one client connection
*/
void handleClient(int newsockfd) {

    char buffer[BUFFER_SIZE];
    char token_buffer[TOKEN_BUFFER_SIZE];

    int n;
    int msgRead = 0;
    int tb_index = 0;


    int data[3]; // x, y, color
    int state = 0;

    bzero(buffer,BUFFER_SIZE);
    bzero(token_buffer, TOKEN_BUFFER_SIZE);
    bzero(data,3*sizeof(int));

    while ((n = read(newsockfd,buffer,BUFFER_SIZE -1)) > 0) {
        for (int i = 0;i<BUFFER_SIZE -1;i++) {
            if (isxdigit(buffer[i])) {
                token_buffer[tb_index++] = buffer[i];
            } else if (tb_index > 0) {
                if (state == 2)
                    data[state++] = (int)strtol(token_buffer, NULL, 16);
                else
                    data[state++] = atoi(token_buffer);
                tb_index = 0;
                bzero(token_buffer, TOKEN_BUFFER_SIZE);
                if (state == 3) {
                    printf("set RGB %d %d %d\n",data[0], data[1], data[2]);
                    state = 0;
                }
            }
        }
        //n = write(newsockfd,"I got your message\n\n",18);
        //if (n < 0) error("ERROR writing to socket");
        bzero(buffer,BUFFER_SIZE);
        msgRead = 1;
    }

    if (!msgRead)
        error("ERROR, could not read from socket, client disconnected\n.");
    else {
        printf("Message processed, client disconnected.\n");
    }

}

int main(int argc, char *argv[])
{
     int sockfd, newsockfd, portno;
     socklen_t clilen;
    
     struct sockaddr_in serv_addr, cli_addr;
     int pid;
     if (argc < 2)
         error("ERROR, please specify a port number.\n");

     sockfd = socket(AF_INET, SOCK_STREAM, 0);
     if (sockfd < 0) 
        error("ERROR, socket could not be openend");

     bzero((char *) &serv_addr, sizeof(serv_addr));
     portno = atoi(argv[1]);

     serv_addr.sin_family = AF_INET;
     serv_addr.sin_addr.s_addr = INADDR_ANY;
     serv_addr.sin_port = htons(portno);

     if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
        error("ERROR, could not bind socket, port already in use?");

     listen(sockfd,5);
     clilen = sizeof(cli_addr);
	 
	 printf("PI Image server started on port: %d ...\n", portno);

	 while(1) {
       newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
       if (newsockfd < 0) 
            error("ERROR, could not accept connection.");
			
		 pid = fork();
	     if (pid < 0)
	       error("ERROR, could not fork client threaad.");
	     if (pid == 0)
	     {
	       close(sockfd);
	       handleClient(newsockfd);
	       close(newsockfd);
	       exit(0);
	     }
	     else
	       close(newsockfd);
	 }
     return 0; 
}
