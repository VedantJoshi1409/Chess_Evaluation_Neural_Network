import torch
from torch.utils.data import DataLoader, Dataset
from torch import nn
import torchmetrics


def read_file(amountLines, filename, startLine=1):
    data = []
    targets = []
    counter = 0
    startCounter = 0
    with open(filename, 'r') as file:
        for line in file:
            startCounter += 1
            if startCounter < startLine:
                continue
            counter += 1
            if counter > amountLines:
                break
            if counter % 100 == 0:
                print(f"Processed {counter}")

            currentData = torch.zeros([81920])
            line = line.strip().split(',')
            # Convert strings to integers
            line = [int(num) for num in line]
            # Extract the indexes and target
            indexes = line[:-1]
            target = line[-1]
            # Create an array with 0s and set the indexes to 1
            for idx in indexes:
                currentData[idx] = 1
            data.append(currentData)
            targets.append(target)
    return torch.stack(data), torch.sigmoid(torch.tensor(targets) / 410)  # converting centi pawn value to WDL value


def trainingLoop(model, loss_fn, optimizer, epochs, dataloader):
    model.train()
    for epoch in range(epochs):
        train_loss = 0
        for batch, (X, y) in enumerate(dataloader):
            y_pred = model(X)
            loss = loss_fn(y_pred, y)
            train_loss += loss
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
        print(f"Epoch {epoch}, Loss: {train_loss / len(dataloader)}\n")


class ChessDataset(Dataset):
    def __init__(self, amountLines):
        self.data, self.targets = read_file(amountLines, 'tensors.txt')

    def __len__(self):
        return len(self.targets)

    def __getitem__(self, idx):
        return self.data[idx], self.targets[idx]


class ChessNet(nn.Module):
    def __init__(self):
        super().__init__()
        self.layer1 = nn.Linear(40960, 4)
        self.layer2 = nn.Linear(40960, 4)
        self.layer3 = nn.Linear(8, 8)
        self.layer4 = nn.Linear(8, 1)

    def forward(self, x):
        input1 = x[:, :40960]
        input2 = x[:, 40960:]
        output1 = self.layer1(input1)
        output2 = self.layer2(input2)
        output3 = torch.clamp(torch.cat((output1, output2), 1), 0, 1)
        output4 = torch.clamp(self.layer3(output3), 0, 1)
        output5 = self.layer4(output4)
        return output5.squeeze()


model = ChessNet()
ds = ChessDataset(10000)  # amount of lines
dl = DataLoader(ds, batch_size=32, shuffle=True, drop_last=True)
loss_fn = nn.MSELoss()  # mean squared loss
optimizer = torch.optim.SGD(model.parameters(), lr=0.1, momentum=0.9)  # stochastic gradient descent
trainingLoop(model, loss_fn, optimizer, 4, dl)
torch.save(model.state_dict(), f="ChessNet.pth")

# model.load_state_dict(torch.load("ChessNet.pth"))
# model.eval()
# testdata, testtargets = read_file(1, 'tensors.txt', 13000)
# with torch.inference_mode():
#     y_preds = model(testdata)
#     print(y_preds, testtargets)
