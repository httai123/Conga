import os
import glob

modules = glob.glob(os.path.join(os.path.dirname(__file__)+'/*.py'))
__all__ = [os.path.basename(f)[:-3] for f in modules]


print('Các module đã được thêm vào:', __all__)
