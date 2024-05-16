
# Chess_Evaluation_Neural_Network
Neural network architecture for training a neural network to evaluate chess positions, based on research from Stockfish

## Usage 
Get data from [here](https://github.com/VedantJoshi1409/LiChessDb_to_Tensor/tree/main). Set the dataset to load from the data file. Customize learning rate, loss function, optimizer, and more to your heart's content. Run the code and the trained network will save to "ChessNet.pth".

## Architecture 
Based on [research](https://github.com/official-stockfish/nnue-pytorch/blob/master/docs/nnue.md#feature-set) from Stockfish
![HalfKP[40960]->4x2->8->1](https://github.com/official-stockfish/nnue-pytorch/raw/master/docs/img/HalfKP-40960-4x2-8-1.svg)\
- The 81920 inputs get split into 2 sets of 40960. 
- In the first layer, the first set of inputs goes through the first sets of weights and combines into 4 neurons while the second set goes the the second set of weights and also combines into 4 neurons. 
- A Clipped ReLU activation function is applied on these neurons. 
- They are then stacked into a 8 neurons which pass through the second layer of size 8. Another Clipped ReLU is applied and the neurons pass through a final layer of size 1.  
- The remaining neuron is the evaluation of the position. 

For information about the input format, go [here](https://github.com/official-stockfish/nnue-pytorch/blob/master/docs/nnue.md#feature-set) for the official documentation or [here](https://github.com/VedantJoshi1409/LiChessDb_to_Tensor/tree/main) for my explanation.
