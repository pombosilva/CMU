# CMU Project

## Connect to virtual machine
  ```ssh -i <key_file_location> ubuntu@192.92.147.96```
  
## Launch Webserver

  ```cd && cd Backend && python3 LibraryBackend.py ```
  
  
  ### Transfer Files over SCP
  ``` scp -r -i <key_file_location> <name_of_file> ubuntu@192.92.147.96:<destination_directory_inside_vm> ```
  
  example  :  ``` scp -r -i C:\Users\Joao\Desktop\Tecnico\1Ano\P4\MUC\Project\keys\cmu_22_23.pem .\LibraryBackend.py ubuntu@192.92.147.96:/home/ubuntu/Project ```
